import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExecutionResult {
    private final boolean success;
    private final String message;
    private final String runtimeOutput;
    private final Map<String, Integer> classByteSizes;

    public ExecutionResult(boolean success, String message, String runtimeOutput, Map<String, Integer> classByteSizes) {
        this.success = success;
        this.message = message;
        this.runtimeOutput = runtimeOutput;
        this.classByteSizes = new LinkedHashMap<>(classByteSizes);
    }

    public static ExecutionResult fail(String message) {
        return new ExecutionResult(false, message, "", Collections.emptyMap());
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getRuntimeOutput() {
        return runtimeOutput;
    }

    public Map<String, Integer> getClassByteSizes() {
        return classByteSizes;
    }

    public String toPlainText() {
        StringBuilder sb = new StringBuilder();
        sb.append("success: ").append(success).append('\n');
        sb.append("message: ").append(message).append('\n');

        if (!classByteSizes.isEmpty()) {
            sb.append("bytecode sizes:").append('\n');
            for (Map.Entry<String, Integer> entry : classByteSizes.entrySet()) {
                sb.append("  - ")
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append(" bytes")
                        .append('\n');
            }
        }

        if (!runtimeOutput.isEmpty()) {
            sb.append("runtime output:").append('\n');
            sb.append(runtimeOutput).append('\n');
        }

        return sb.toString();
    }
}
