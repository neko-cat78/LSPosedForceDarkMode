package de.robv.android.xposed;

public class XC_MethodHook {

    public static class MethodHookParam {
        public Object thisObject;
        public Object[] args;
        private Object result;
        private boolean hasResult;

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
            this.hasResult = true;
        }

        public boolean hasResult() {
            return hasResult;
        }

        public Object getObjectExtra(String key) { return null; }
        public void setObjectExtra(String key, Object o) {}
    }

    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {}
}
