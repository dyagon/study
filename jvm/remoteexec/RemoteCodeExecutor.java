import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RemoteCodeExecutor {
    private static final int EXECUTION_TIMEOUT_SECONDS = 5;

    private final InMemoryJavaCompiler compiler = new InMemoryJavaCompiler();

    public ExecutionResult execute(String className, String sourceCode) {
        if (className == null || className.isBlank()) {
            return ExecutionResult.fail("className is empty");
        }
        if (sourceCode == null || sourceCode.isBlank()) {
            return ExecutionResult.fail("sourceCode is empty");
        }

        InMemoryJavaCompiler.CompileResult compileResult = compiler.compile(className, sourceCode);
        if (!compileResult.isSuccess()) {
            return ExecutionResult.fail("Compile failed:\n" + compileResult.getMessage());
        }

        Map<String, byte[]> classBytes = compileResult.getClassBytes();
        Map<String, Integer> classSizeMap = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : classBytes.entrySet()) {
            classSizeMap.put(entry.getKey(), entry.getValue().length);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> future = executor.submit(new ExecutionTask(className, classBytes));
            String runtimeOutput = future.get(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return new ExecutionResult(true, "Compile and execute success", runtimeOutput, classSizeMap);
        } catch (TimeoutException e) {
            return new ExecutionResult(false, "Execution timeout after " + EXECUTION_TIMEOUT_SECONDS + " seconds", "", classSizeMap);
        } catch (ExecutionException e) {
            String message = e.getCause() == null ? e.getMessage() : e.getCause().toString();
            return new ExecutionResult(false, "Execution failed: " + message, "", classSizeMap);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ExecutionResult(false, "Execution interrupted", "", classSizeMap);
        } finally {
            executor.shutdownNow();
        }
    }

    private static class ExecutionTask implements Callable<String> {
        private final String className;
        private final Map<String, byte[]> classBytes;

        ExecutionTask(String className, Map<String, byte[]> classBytes) {
            this.className = className;
            this.classBytes = classBytes;
        }

        @Override
        public String call() throws Exception {
            MemoryClassLoader loader = new MemoryClassLoader(classBytes);
            Class<?> clazz = loader.loadClass(className);

            Method method;
            try {
                method = clazz.getMethod("execute");
                if (!String.class.equals(method.getReturnType())) {
                    throw new NoSuchMethodException("execute() must return java.lang.String");
                }
                Object result = method.invoke(null);
                return result == null ? "" : result.toString();
            } catch (NoSuchMethodException ignored) {
                // Fallback to main(String[] args) and capture System.out.
            }

            Method mainMethod = clazz.getMethod("main", String[].class);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
            try {
                System.setOut(ps);
                System.setErr(ps);
                mainMethod.invoke(null, (Object) new String[0]);
            } finally {
                System.setOut(oldOut);
                System.setErr(oldErr);
                ps.close();
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static class MemoryClassLoader extends ClassLoader {
        private final Map<String, byte[]> classBytes;

        MemoryClassLoader(Map<String, byte[]> classBytes) {
            super(RemoteCodeExecutor.class.getClassLoader());
            this.classBytes = classBytes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classBytes.get(name);
            if (bytes == null) {
                return super.findClass(name);
            }
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
