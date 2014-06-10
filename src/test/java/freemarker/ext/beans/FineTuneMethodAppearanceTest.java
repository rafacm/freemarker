package freemarker.ext.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import freemarker.ext.beans.BeansWrapper.MethodAppearanceDecision;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

@RunWith(JUnit4.class)
public class FineTuneMethodAppearanceTest {

    @Test
    public void detectNoOwerride() throws TemplateModelException {
        assertNull(new DefaultObjectWrapper().getMethodAppearanceFineTuner());
        assertNull(new DefaultObjectWrapperNoOverride().getMethodAppearanceFineTuner());
        assertNull(new DefaultObjectWrapperNoOverrideExt().getMethodAppearanceFineTuner());
    }
    
    @Test
    public void legacyWayOfConfiguring() throws TemplateModelException {
        DefaultObjectWrapper ow = new DefaultObjectWrapperOverride();
        ow.setExposeFields(true);
        checkIfProperlyWrapped(ow.wrap(new C()));
        
        ow = new DefaultObjectWrapperOverrideExt();
        ow.setExposeFields(true);
        checkIfProperlyWrapped(ow.wrap(new C()));
    }

    @Test
    public void newWayOfConfiguring() throws TemplateModelException {
        DefaultObjectWrapper ow = new DefaultObjectWrapper();
        ow.setMethodAppearanceFineTuner(GetlessMethodsAsPropertyGettersRule.INSTANCE);
        ow.setExposeFields(true);
        checkIfProperlyWrapped(ow.wrap(new C()));
    }
    
    private void checkIfProperlyWrapped(TemplateModel tm) throws TemplateModelException {
        TemplateHashModel thm = (TemplateHashModel) tm;
        assertEquals("v1", ((TemplateScalarModel) thm.get("v1")).getAsString());
        assertEquals("v2()", ((TemplateScalarModel) thm.get("v2")).getAsString());
        assertEquals("getV3()", ((TemplateScalarModel) thm.get("v3")).getAsString());
        assertTrue(thm.get("getV3") instanceof TemplateMethodModelEx);
    }
    
    static public class C {
        
        public String v1 = "v1";

        public String v2 = "v2";
        public String v2() { return "v2()"; }
        
        public String v3() { return "v3()"; }
        public String getV3() { return "getV3()"; }
    }
    
    static class DefaultObjectWrapperNoOverride extends DefaultObjectWrapper {
        
    }

    static class DefaultObjectWrapperNoOverrideExt extends DefaultObjectWrapperNoOverride { }
    
    static class DefaultObjectWrapperOverride extends DefaultObjectWrapper {

        @Override
        protected void finetuneMethodAppearance(Class clazz, Method m, MethodAppearanceDecision out) {
            GetlessMethodsAsPropertyGettersRule.INSTANCE.legacyProcess(clazz, m, out);
        }
        
    }
    
    static class DefaultObjectWrapperOverrideExt extends DefaultObjectWrapperOverride { }
}