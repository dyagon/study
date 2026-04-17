import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InMemoryJavaCompiler {
    public CompileResult compile(String className, String sourceCode) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return CompileResult.fail("No JavaCompiler found. Please run with a JDK, not a JRE.");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), StandardCharsets.UTF_8);
        MemoryFileManager memoryFileManager = new MemoryFileManager(standardFileManager);

        List<String> options = Arrays.asList("-g", "-encoding", "UTF-8");
        JavaFileObject sourceObject = new StringSourceJavaFileObject(className, sourceCode);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                memoryFileManager,
                diagnostics,
                options,
                null,
                Arrays.asList(sourceObject)
        );

        boolean success = Boolean.TRUE.equals(task.call());

        try {
            memoryFileManager.close();
        } catch (IOException ignore) {
            // Close failures are non-fatal for this demo.
        }

        if (!success) {
            return CompileResult.fail(diagnosticsToText(diagnostics));
        }

        return CompileResult.ok(memoryFileManager.getClassBytes());
    }

    private String diagnosticsToText(DiagnosticCollector<JavaFileObject> diagnostics) {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
            sb.append(d.getKind())
                    .append(" at line ")
                    .append(d.getLineNumber())
                    .append(": ")
                    .append(d.getMessage(Locale.getDefault()))
                    .append('\n');
        }
        return sb.toString();
    }

    static class CompileResult {
        private final boolean success;
        private final String message;
        private final Map<String, byte[]> classBytes;

        private CompileResult(boolean success, String message, Map<String, byte[]> classBytes) {
            this.success = success;
            this.message = message;
            this.classBytes = classBytes;
        }

        static CompileResult ok(Map<String, byte[]> classBytes) {
            return new CompileResult(true, "Compile success", classBytes);
        }

        static CompileResult fail(String message) {
            return new CompileResult(false, message, new LinkedHashMap<>());
        }

        boolean isSuccess() {
            return success;
        }

        String getMessage() {
            return message;
        }

        Map<String, byte[]> getClassBytes() {
            return classBytes;
        }
    }

    private static class StringSourceJavaFileObject extends SimpleJavaFileObject {
        private final String sourceCode;

        StringSourceJavaFileObject(String className, String sourceCode) {
            super(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
            this.sourceCode = sourceCode;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return sourceCode;
        }
    }

    private static class ByteArrayJavaFileObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ByteArrayJavaFileObject(String className, Kind kind) {
            super(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind);
        }

        @Override
        public OutputStream openOutputStream() {
            return outputStream;
        }

        byte[] getBytes() {
            return outputStream.toByteArray();
        }
    }

    private static class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final Map<String, ByteArrayJavaFileObject> outputMap = new LinkedHashMap<>();

        MemoryFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location,
                                                   String className,
                                                   JavaFileObject.Kind kind,
                                                   FileObject sibling) {
            ByteArrayJavaFileObject fileObject = new ByteArrayJavaFileObject(className, kind);
            outputMap.put(className, fileObject);
            return fileObject;
        }

        Map<String, byte[]> getClassBytes() {
            Map<String, byte[]> result = new LinkedHashMap<>();
            for (Map.Entry<String, ByteArrayJavaFileObject> entry : outputMap.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getBytes());
            }
            return result;
        }
    }
}
