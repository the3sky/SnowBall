package blue.stack.snowball.app.lockscreen;

public interface LockScreenListener {
    void onLockScreenStarted(LockScreenManager lockScreenManager);

    void onLockScreenStopped(LockScreenManager lockScreenManager);

    void onPhoneCall(LockScreenManager lockScreenManager);

    void onPhoneCallEnded(LockScreenManager lockScreenManager);
}
