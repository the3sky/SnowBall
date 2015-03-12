package blue.stack.snowball.app.logging;

public class RemoteDiagnostics {
    StringBuffer buffer;

    public RemoteDiagnostics() {
        this.buffer = new StringBuffer();
    }

    public void addDiagnostic(String tag, String key, String value) {
        this.buffer.append("[");
        this.buffer.append(tag);
        this.buffer.append("] ");
        this.buffer.append(key);
        this.buffer.append(" = ");
        this.buffer.append(value);
        this.buffer.append("\r\n");
    }

    public void addDiagnostic(String tag, String key, int value) {
        addDiagnostic(tag, key, Integer.toString(value));
    }

    public void addDiagnostic(String tag, String key, boolean value) {
        addDiagnostic(tag, key, Boolean.toString(value));
    }

    public void addDiagnostic(String tag, String key, long value) {
        addDiagnostic(tag, key, Long.toString(value));
    }

    public String toString() {
        return this.buffer.toString();
    }
}
