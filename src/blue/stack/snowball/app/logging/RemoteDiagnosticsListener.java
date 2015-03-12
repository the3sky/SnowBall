package blue.stack.snowball.app.logging;

public interface RemoteDiagnosticsListener {
    void onRemoteDiagnosticsRequested(RemoteDiagnostics remoteDiagnostics);
}
