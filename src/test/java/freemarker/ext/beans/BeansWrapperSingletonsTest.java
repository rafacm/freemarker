package freemarker.ext.beans;

import java.lang.ref.Reference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import freemarker.ext.beans.BeansWrapper.MethodAppearanceDecision;
import freemarker.ext.beans.BeansWrapper.MethodAppearanceDecisionInput;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleObjectWrapper;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.Version;
import freemarker.template._TemplateAPI;

public class BeansWrapperSingletonsTest extends TestCase {

    private static final Version V_2_3_0 = new Version(2, 3, 0);
    private static final Version V_2_3_19 = new Version(2, 3, 19);
    private static final Version V_2_3_21 = new Version(2, 3, 21);

    public BeansWrapperSingletonsTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        BeansWrapperBuilder.clearInstanceCache();  // otherwise ClassInrospector cache couldn't become cleared in reality
        _TemplateAPI.DefaultObjectWrapperFactory_clearInstanceCache();
        BeansWrapperBuilder.clearInstanceCache();
    }

    public void testBeansWrapperFactoryEquals() throws Exception {
        assertEquals(V_2_3_21, new BeansWrapperBuilder(V_2_3_21).getIncompatibleImprovements());
        assertEquals(new Version(2, 3, 0), new BeansWrapperBuilder(new Version(2, 3, 20)).getIncompatibleImprovements());
        try {
            new BeansWrapperBuilder(new Version(2, 3, 22));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("upgrade"));
        }

        BeansWrapperBuilder pa1;
        BeansWrapperBuilder pa2;
        
        pa1 = new BeansWrapperBuilder(V_2_3_21);
        pa2 = new BeansWrapperBuilder(V_2_3_21);
        assertEquals(pa1, pa2);
        
        pa1.setSimpleMapWrapper(true);
        assertNotEquals(pa1, pa2);
        assertFalse(pa1.hashCode() == pa2.hashCode());
        pa2.setSimpleMapWrapper(true);
        assertEquals(pa1, pa2);
        assertTrue(pa1.hashCode() == pa2.hashCode());
        
        pa1.setExposeFields(true);
        assertNotEquals(pa1, pa2);
        assertFalse(pa1.hashCode() == pa2.hashCode());
        pa2.setExposeFields(true);
        assertEquals(pa1, pa2);
        assertTrue(pa1.hashCode() == pa2.hashCode());
        
        pa1.setExposureLevel(0);
        assertNotEquals(pa1, pa2);
        assertFalse(pa1.hashCode() == pa2.hashCode());
        pa2.setExposureLevel(0);
        assertEquals(pa1, pa2);
        assertTrue(pa1.hashCode() == pa2.hashCode());
        
        pa1.setExposureLevel(1);
        assertNotEquals(pa1, pa2);
        assertFalse(pa1.hashCode() == pa2.hashCode());
        pa2.setExposureLevel(1);
        assertEquals(pa1, pa2);
        assertTrue(pa1.hashCode() == pa2.hashCode());
        
        pa1.setDefaultDateType(TemplateDateModel.DATE);
        assertNotEquals(pa1, pa2);
        pa2.setDefaultDateType(TemplateDateModel.DATE);
        assertEquals(pa1, pa2);
        assertTrue(pa1.hashCode() == pa2.hashCode());
        
        pa1.setStrict(true);
        assertNotEquals(pa1, pa2);
        assertFalse(pa1.hashCode() == pa2.hashCode());
        pa2.setStrict(true);
        assertEquals(pa1, pa2);
        assertTrue(pa1.hashCode() == pa2.hashCode());

        pa1.setUseModelCache(true);
        assertNotEquals(pa1, pa2);
        assertFalse(pa1.hashCode() == pa2.hashCode());
        pa2.setUseModelCache(true);
        assertEquals(pa1, pa2);
        assertTrue(pa1.hashCode() == pa2.hashCode());
        
        AlphabeticalMethodSorter ms = new AlphabeticalMethodSorter(true);
        pa1.setMethodSorter(ms);
        assertNotEquals(pa1, pa2);
        pa2.setMethodSorter(ms);
        assertEquals(pa1, pa2);
        assertTrue(pa1.hashCode() == pa2.hashCode());
        
        MethodAppearanceFineTuner maft = new MethodAppearanceFineTuner() {
            public void process(MethodAppearanceDecisionInput in, MethodAppearanceDecision out) { }
        };
        pa1.setMethodAppearanceFineTuner(maft);
        assertNotEquals(pa1, pa2);
        pa2.setMethodAppearanceFineTuner(maft);
        assertEquals(pa1, pa2);
        assertTrue(pa1.hashCode() == pa2.hashCode());
    }
    
    public void testBeansWrapperFactoryProducts() throws Exception {
        List<BeansWrapper> hardReferences = new LinkedList<BeansWrapper>();
        
        assertEquals(0, getBeansWrapperInstanceCacheSize());
        
        {
            BeansWrapper bw = getBeansWrapperWithSetting(V_2_3_19, true);
            assertEquals(1, getBeansWrapperInstanceCacheSize());
            assertSame(bw.getClass(), BeansWrapper.class);
            assertEquals(new Version(2, 3, 0), bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertTrue(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof SimpleMapModel);
            assertFalse(bw.isStrict());
            assertFalse(bw.getUseCache());
            assertEquals(TemplateDateModel.UNKNOWN, bw.getDefaultDateType());
            assertSame(bw, bw.getOuterIdentity());
            assertTrue(bw.isClassIntrospectionCacheRestricted());
            assertNull(bw.getMethodAppearanceFineTuner());
            assertNull(bw.getMethodSorter());
            
            try {
                bw.setExposeFields(true);  // can't modify the settings of a (potential) singleton
                fail();
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("modify"));
            }
            
            assertSame(bw, getBeansWrapperWithSetting(new Version(2, 3, 20), true));
            assertSame(bw, getBeansWrapperWithSetting(new Version(2, 3, 0), true));
            assertEquals(1, getBeansWrapperInstanceCacheSize());
            
            BeansWrapperBuilder factory = new BeansWrapperBuilder(new Version(2, 3, 5));
            factory.setSimpleMapWrapper(true);
            assertSame(bw, factory.getResult());
            assertEquals(1, getBeansWrapperInstanceCacheSize());
            
            hardReferences.add(bw);            
        }
        
        {
            BeansWrapper bw = getBeansWrapperWithSetting(V_2_3_21, true);
            assertEquals(2, getBeansWrapperInstanceCacheSize());
            assertSame(bw.getClass(), BeansWrapper.class);
            assertEquals(V_2_3_21, bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertTrue(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof SimpleMapModel);
            assertTrue(bw.isClassIntrospectionCacheRestricted());
            assertNull(bw.getMethodAppearanceFineTuner());
            assertNull(bw.getMethodSorter());
            
            BeansWrapperBuilder factory = new BeansWrapperBuilder(V_2_3_21);
            factory.setSimpleMapWrapper(true);
            assertSame(bw, factory.getResult());
            assertEquals(2, getBeansWrapperInstanceCacheSize());
            
            hardReferences.add(bw);            
        }
        
        {
            // Again... same as the very first
            BeansWrapper bw = getBeansWrapperWithSetting(V_2_3_19, true);
            assertEquals(2, getBeansWrapperInstanceCacheSize());
            assertEquals(new Version(2, 3, 0), bw.getIncompatibleImprovements());
        }
        
        {
            BeansWrapper bw = getBeansWrapperWithSetting(V_2_3_19, false);
            assertEquals(3, getBeansWrapperInstanceCacheSize());
            assertSame(bw.getClass(), BeansWrapper.class);
            assertEquals(new Version(2, 3, 0), bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertFalse(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof MapModel);
            
            assertSame(bw, getBeansWrapperWithSetting(new Version(2, 3, 20), false));
            assertSame(bw, getBeansWrapperWithSetting(new Version(2, 3, 0), false));
            assertSame(bw, new BeansWrapperBuilder(new Version(2, 3, 5)).getResult());
            assertEquals(3, getBeansWrapperInstanceCacheSize());
            
            hardReferences.add(bw);            
        }
        
        {
            BeansWrapper bw = getBeansWrapperWithSetting(V_2_3_21, false);
            assertEquals(4, getBeansWrapperInstanceCacheSize());
            assertSame(bw.getClass(), BeansWrapper.class);
            assertEquals(V_2_3_21, bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertFalse(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof MapModel);
            
            assertSame(bw, new BeansWrapperBuilder(new Version(2, 3, 21)).getResult());
            assertEquals(4, getBeansWrapperInstanceCacheSize());
            
            hardReferences.add(bw);            
        }

        {
            // Again... same as earlier
            BeansWrapper bw = getBeansWrapperWithSetting(V_2_3_21, true);
            assertEquals(V_2_3_21, bw.getIncompatibleImprovements());
            assertTrue(bw.isSimpleMapWrapper());
            assertEquals(4, getBeansWrapperInstanceCacheSize());
        }
        
        {
            BeansWrapperBuilder factory = new BeansWrapperBuilder(V_2_3_19);
            factory.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
            BeansWrapper bw = factory.getResult();
            BeansWrapper bw2 = factory.getResult();
            assertEquals(5, getBeansWrapperInstanceCacheSize());
            assertSame(bw, bw2);
            
            assertSame(bw.getClass(), BeansWrapper.class);
            assertEquals(V_2_3_0, bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertFalse(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof MapModel);
            assertEquals(BeansWrapper.EXPOSE_PROPERTIES_ONLY, bw.getExposureLevel());
            assertFalse(bw.isStrict());
            assertEquals(TemplateDateModel.UNKNOWN, bw.getDefaultDateType());
            assertSame(bw, bw.getOuterIdentity());
            
            hardReferences.add(bw);            
        }
        
        {
            BeansWrapperBuilder factory = new BeansWrapperBuilder(V_2_3_19);
            factory.setExposeFields(true);
            BeansWrapper bw = factory.getResult();
            BeansWrapper bw2 = factory.getResult();
            assertEquals(6, getBeansWrapperInstanceCacheSize());
            assertSame(bw, bw2);
            
            assertSame(bw.getClass(), BeansWrapper.class);
            assertEquals(V_2_3_0, bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertFalse(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof MapModel);
            assertTrue(bw.isExposeFields());
            
            hardReferences.add(bw);            
        }
        
        {
            BeansWrapperBuilder factory = new BeansWrapperBuilder(V_2_3_19);
            factory.setStrict(true);
            factory.setDefaultDateType(TemplateDateModel.DATETIME);
            factory.setOuterIdentity(new SimpleObjectWrapper());
            BeansWrapper bw = factory.getResult();
            assertEquals(7, getBeansWrapperInstanceCacheSize());
            assertTrue(bw.isStrict());
            assertEquals(TemplateDateModel.DATETIME, bw.getDefaultDateType());
            assertSame(SimpleObjectWrapper.class, bw.getOuterIdentity().getClass());
            
            hardReferences.add(bw);            
        }
        
        // Effect of reference and cache clearings:
        {
            BeansWrapper bw1 = new BeansWrapperBuilder(V_2_3_21).getResult();
            assertEquals(7, getBeansWrapperInstanceCacheSize());
            assertEquals(7, getBeansWrapperNonClearedInstanceCacheSize());
            
            clearBeansWrapperInstanceCacheReferences(false);
            assertEquals(7, getBeansWrapperInstanceCacheSize());
            assertEquals(0, getBeansWrapperNonClearedInstanceCacheSize());
            
            BeansWrapper bw2 = new BeansWrapperBuilder(V_2_3_21).getResult();
            assertNotSame(bw1, bw2);
            assertEquals(7, getBeansWrapperInstanceCacheSize());
            assertEquals(1, getBeansWrapperNonClearedInstanceCacheSize());
            
            assertSame(bw2, new BeansWrapperBuilder(V_2_3_21).getResult());
            assertEquals(1, getBeansWrapperNonClearedInstanceCacheSize());
            
            clearBeansWrapperInstanceCacheReferences(true);
            BeansWrapper bw3 = new BeansWrapperBuilder(V_2_3_21).getResult();
            assertNotSame(bw2, bw3);
            assertEquals(1, getBeansWrapperInstanceCacheSize());
            assertEquals(1, getBeansWrapperNonClearedInstanceCacheSize());
        }

        {
            BeansWrapperBuilder factory = new BeansWrapperBuilder(V_2_3_19);
            factory.setUseModelCache(true);
            BeansWrapper bw = factory.getResult();
            assertTrue(bw.getUseCache());
            assertEquals(2, getBeansWrapperInstanceCacheSize());
            
            hardReferences.add(bw);            
        }
        
        assertTrue(hardReferences.size() != 0);  // just to save it from GC until this line        
    }
    
    private BeansWrapper getBeansWrapperWithSetting(Version ici, boolean simpleMapWrapper) {
        BeansWrapperBuilder f = new BeansWrapperBuilder(ici);
        f.setSimpleMapWrapper(simpleMapWrapper);
        return f.getResult();
    }

    public void testMultipleTCCLs() {
        List<BeansWrapper> hardReferences = new LinkedList<BeansWrapper>();
        
        assertEquals(0, getBeansWrapperInstanceCacheSize());
        
        BeansWrapper bw1 = new BeansWrapperBuilder(V_2_3_19).getResult();
        assertEquals(1, getBeansWrapperInstanceCacheSize());
        hardReferences.add(bw1);
        
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        // Doesn't mater what, just be different from oldTCCL: 
        ClassLoader newTCCL = oldTCCL == null ? this.getClass().getClassLoader() : null;
        
        BeansWrapper bw2;
        Thread.currentThread().setContextClassLoader(newTCCL);
        try {
            bw2 = new BeansWrapperBuilder(V_2_3_19).getResult();
            assertEquals(2, getBeansWrapperInstanceCacheSize());
            hardReferences.add(bw2);
            
            assertNotSame(bw1, bw2);
            assertSame(bw2, new BeansWrapperBuilder(V_2_3_19).getResult());
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        
        assertSame(bw1, new BeansWrapperBuilder(V_2_3_19).getResult());
        assertEquals(2, getBeansWrapperInstanceCacheSize());

        BeansWrapper bw3;
        Thread.currentThread().setContextClassLoader(newTCCL);
        try {
            assertSame(bw2, new BeansWrapperBuilder(V_2_3_19).getResult());
            
            bw3 = new BeansWrapperBuilder(V_2_3_21).getResult();
            assertEquals(3, getBeansWrapperInstanceCacheSize());
            hardReferences.add(bw3);
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        
        BeansWrapper bw4 = new BeansWrapperBuilder(V_2_3_21).getResult();
        assertEquals(4, getBeansWrapperInstanceCacheSize());
        hardReferences.add(bw4);
        
        assertNotSame(bw3, bw4);
        
        assertTrue(hardReferences.size() != 0);  // just to save it from GC until this line        
    }

    public void testDefaultObjectWrapperFactoryProducts() throws Exception {
        {
            DefaultObjectWrapperBuilder factory = new DefaultObjectWrapperBuilder(V_2_3_19);
            factory.setSimpleMapWrapper(true);
            BeansWrapper bw = factory.getResult();
            assertSame(bw, factory.getResult());
            assertSame(bw.getClass(), DefaultObjectWrapper.class);
            assertEquals(V_2_3_0, bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertTrue(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof SimpleHash);
            assertTrue(bw.isClassIntrospectionCacheRestricted());
        }
        
        {
            DefaultObjectWrapperBuilder factory = new DefaultObjectWrapperBuilder(Configuration.getVersion());
            factory.setSimpleMapWrapper(true);
            BeansWrapper bw = factory.getResult();
            assertSame(bw, factory.getResult());
            assertSame(bw.getClass(), DefaultObjectWrapper.class);
            assertEquals(
                    BeansWrapper.normalizeIncompatibleImprovementsVersion(Configuration.getVersion()),
                    bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertTrue(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof SimpleHash);
        }
        
        {
            BeansWrapper bw = new DefaultObjectWrapperBuilder(V_2_3_19).getResult();
            assertSame(bw.getClass(), DefaultObjectWrapper.class);
            assertEquals(new Version(2, 3, 0), bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertFalse(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof SimpleHash);
            
            assertSame(bw, new DefaultObjectWrapperBuilder(new Version(2, 3, 20)).getResult());
            assertSame(bw, new DefaultObjectWrapperBuilder(new Version(2, 3, 0)).getResult());
            assertSame(bw, new DefaultObjectWrapperBuilder(new Version(2, 3, 5)).getResult());
        }
        
        {
            BeansWrapper bw = new DefaultObjectWrapperBuilder(V_2_3_21).getResult();
            assertSame(bw.getClass(), DefaultObjectWrapper.class);
            assertEquals(V_2_3_21, bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertFalse(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof SimpleHash);
            assertTrue(bw.isClassIntrospectionCacheRestricted());
            
            assertSame(bw, new DefaultObjectWrapperBuilder(new Version(2, 3, 21)).getResult());
        }

        {
            BeansWrapper bw = new DefaultObjectWrapperBuilder(V_2_3_19).getResult();
            assertEquals(new Version(2, 3, 0), bw.getIncompatibleImprovements());
        }
        
        {
            DefaultObjectWrapperBuilder factory = new DefaultObjectWrapperBuilder(V_2_3_19);
            factory.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
            BeansWrapper bw = factory.getResult();
            BeansWrapper bw2 = factory.getResult();
            assertSame(bw, bw2);  // not cached
            
            assertSame(bw.getClass(), DefaultObjectWrapper.class);
            assertEquals(V_2_3_0, bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertFalse(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof SimpleHash);
            assertEquals(BeansWrapper.EXPOSE_PROPERTIES_ONLY, bw.getExposureLevel());
        }
        
        {
            DefaultObjectWrapperBuilder factory = new DefaultObjectWrapperBuilder(V_2_3_19);
            factory.setExposeFields(true);
            BeansWrapper bw = factory.getResult();
            BeansWrapper bw2 = factory.getResult();
            assertSame(bw, bw2);  // not cached
            
            assertSame(bw.getClass(), DefaultObjectWrapper.class);
            assertEquals(V_2_3_0, bw.getIncompatibleImprovements());
            assertTrue(bw.isWriteProtected());
            assertFalse(bw.isSimpleMapWrapper());
            assertTrue(bw.wrap(new HashMap()) instanceof SimpleHash);
            assertEquals(true, bw.isExposeFields());
        }
    }
    
    public void testClassInrospectorCache() throws TemplateModelException {
        assertFalse(new BeansWrapper().isClassIntrospectionCacheRestricted());
        assertFalse(new BeansWrapper(new Version(2, 3, 21)).isClassIntrospectionCacheRestricted());
        assertTrue(new BeansWrapperBuilder(new Version(2, 3, 20)).getResult().isClassIntrospectionCacheRestricted());
        
        ClassIntrospectorBuilder.clearInstanceCache();
        BeansWrapperBuilder.clearInstanceCache();
        checkClassIntrospectorCacheSize(0);
        
        List<BeansWrapper> hardReferences = new LinkedList<BeansWrapper>();
        BeansWrapperBuilder factory;
        
        {
            factory = new BeansWrapperBuilder(V_2_3_19);
            
            BeansWrapper bw1 = factory.getResult();
            checkClassIntrospectorCacheSize(1);
            
            factory.setExposureLevel(BeansWrapper.EXPOSE_SAFE);  // this was already set to this
            factory.setSimpleMapWrapper(true);  // this shouldn't matter for the introspection cache
            BeansWrapper bw2 = factory.getResult();
            checkClassIntrospectorCacheSize(1);
            
            assertSame(bw2.getClassIntrospector(), bw1.getClassIntrospector());
            assertNotSame(bw1, bw2);
            
            // Wrapping tests:
            assertFalse(exposesFields(bw1));
            assertTrue(exposesProperties(bw1));
            assertTrue(exposesMethods(bw1));
            assertFalse(exposesUnsafe(bw1));
            assertFalse(isSimpleMapWrapper(bw1));
            assertTrue(bw1.isClassIntrospectionCacheRestricted());
            // Prevent introspection cache GC:
            hardReferences.add(bw1);
            hardReferences.add(bw2);
        }
        
        {
            factory = new BeansWrapperBuilder(V_2_3_19);
            factory.setExposeFields(true);
            BeansWrapper bw = factory.getResult();
            checkClassIntrospectorCacheSize(2);
            // Wrapping tests:
            assertTrue(exposesFields(bw));
            assertTrue(exposesProperties(bw));
            assertTrue(exposesMethods(bw));
            assertFalse(exposesUnsafe(bw));
            assertFalse(isSimpleMapWrapper(bw));
            // Prevent introspection cache GC:
            hardReferences.add(bw);
        }

        {
            factory.setExposureLevel(BeansWrapper.EXPOSE_ALL);
            BeansWrapper bw = factory.getResult();
            checkClassIntrospectorCacheSize(3);
            // Wrapping tests:
            assertTrue(exposesFields(bw));
            assertTrue(exposesProperties(bw));
            assertTrue(exposesMethods(bw));
            assertTrue(exposesUnsafe(bw));
            assertFalse(isSimpleMapWrapper(bw));
            // Prevent introspection cache GC:
            hardReferences.add(bw);
        }
        
        {
            factory.setExposeFields(false);
            BeansWrapper bw = factory.getResult();
            checkClassIntrospectorCacheSize(4);
            // Wrapping tests:
            assertFalse(exposesFields(bw));
            assertTrue(exposesProperties(bw));
            assertTrue(exposesMethods(bw));
            assertTrue(exposesUnsafe(bw));
            assertFalse(isSimpleMapWrapper(bw));
            // Prevent introspection cache GC:
            hardReferences.add(bw);
        }
        
        {
            factory.setExposureLevel(BeansWrapper.EXPOSE_NOTHING);
            BeansWrapper bw = factory.getResult();
            checkClassIntrospectorCacheSize(5);
            // Wrapping tests:
            assertFalse(exposesFields(bw));
            assertFalse(exposesProperties(bw));
            assertFalse(exposesMethods(bw));
            assertFalse(exposesUnsafe(bw));
            assertFalse(isSimpleMapWrapper(bw));
            // Prevent introspection cache GC:
            hardReferences.add(bw);
        }

        {
            factory.setExposeFields(true);
            BeansWrapper bw = factory.getResult();
            checkClassIntrospectorCacheSize(6);
            // Wrapping tests:
            assertTrue(exposesFields(bw));
            assertFalse(exposesProperties(bw));
            assertFalse(exposesMethods(bw));
            assertFalse(exposesUnsafe(bw));
            assertFalse(isSimpleMapWrapper(bw));
            // Prevent introspection cache GC:
            hardReferences.add(bw);
        }

        {
            factory.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
            BeansWrapper bw = factory.getResult();
            checkClassIntrospectorCacheSize(7);
            // Wrapping tests:
            assertTrue(exposesFields(bw));
            assertTrue(exposesProperties(bw));
            assertFalse(exposesMethods(bw));
            assertFalse(exposesUnsafe(bw));
            assertFalse(isSimpleMapWrapper(bw));
            // Prevent introspection cache GC:
            hardReferences.add(bw);
        }
        
        {
            factory = new BeansWrapperBuilder(V_2_3_21);
            factory.setExposeFields(false);
            factory.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
            
            BeansWrapper bw1 = factory.getResult();
            checkClassIntrospectorCacheSize(8);
            ClassIntrospector ci1 = bw1.getClassIntrospector();
            
            factory.setSimpleMapWrapper(true);  // Shouldn't mater
            BeansWrapper bw2 = factory.getResult();
            ClassIntrospector ci2 = bw2.getClassIntrospector();
            checkClassIntrospectorCacheSize(8);
            
            assertSame(ci1, ci2);
            assertNotSame(bw1, bw2);
            
            // Wrapping tests:
            assertFalse(exposesFields(bw1));
            assertTrue(exposesProperties(bw1));
            assertFalse(exposesMethods(bw1));
            assertFalse(exposesUnsafe(bw1));
            assertFalse(isSimpleMapWrapper(bw1));
            
            // Prevent introspection cache GC:
            hardReferences.add(bw1);
            hardReferences.add(bw2);
        }
        
        BeansWrapperBuilder.clearInstanceCache();  // otherwise ClassInrospector cache couldn't become cleared in reality
        _TemplateAPI.DefaultObjectWrapperFactory_clearInstanceCache();
        clearClassIntrospectorInstanceCacheReferences(false);
        checkClassIntrospectorCacheSize(8);
        assertEquals(0, getClassIntrospectorNonClearedInstanceCacheSize());

        {
            factory.setSimpleMapWrapper(false);
            factory.setExposeFields(false);
            
            BeansWrapper bw1 = factory.getResult();
            checkClassIntrospectorCacheSize(8);
            assertEquals(1, getClassIntrospectorNonClearedInstanceCacheSize());
            ClassIntrospector ci1 = bw1.getClassIntrospector();
            
            factory.setSimpleMapWrapper(true);  // Shouldn't mater
            BeansWrapper bw2 = factory.getResult();
            ClassIntrospector ci2 = bw2.getClassIntrospector();
            
            assertSame(ci1, ci2);
            assertNotSame(bw1, bw2);
            
            // Wrapping tests:
            assertFalse(exposesFields(bw1));
            assertTrue(exposesProperties(bw1));
            assertFalse(exposesMethods(bw1));
            assertFalse(exposesUnsafe(bw1));
            assertFalse(isSimpleMapWrapper(bw1));
            
            // Prevent introspection cache GC:
            hardReferences.add(bw1);
            hardReferences.add(bw2);
        }
        
        {
            factory = new BeansWrapperBuilder(V_2_3_19);
            BeansWrapper bw = factory.getResult();
            checkClassIntrospectorCacheSize(8);
            assertEquals(2, getClassIntrospectorNonClearedInstanceCacheSize());
            // Wrapping tests:
            assertFalse(exposesFields(bw));
            assertTrue(exposesProperties(bw));
            assertTrue(exposesMethods(bw));
            assertFalse(exposesUnsafe(bw));
            // Prevent introspection cache GC:
            hardReferences.add(bw);
        }

        clearClassIntrospectorInstanceCacheReferences(true);
        checkClassIntrospectorCacheSize(8);
        assertEquals(0, getClassIntrospectorNonClearedInstanceCacheSize());
        
        {
            factory = new BeansWrapperBuilder(V_2_3_21);
            factory.setExposeFields(true);
            BeansWrapper bw = factory.getResult();
            checkClassIntrospectorCacheSize(1);
            // Wrapping tests:
            assertTrue(exposesFields(bw));
            assertTrue(exposesProperties(bw));
            assertTrue(exposesMethods(bw));
            assertFalse(exposesUnsafe(bw));
            // Prevent introspection cache GC:
            hardReferences.add(bw);
        }
        
        {
            factory = new BeansWrapperBuilder(V_2_3_19);
            factory.setMethodAppearanceFineTuner(new MethodAppearanceFineTuner() {
                public void process(MethodAppearanceDecisionInput in, MethodAppearanceDecision out) {
                }
            });  // spoils ClassIntrospector() sharing
            
            BeansWrapper bw1 = factory.getResult();
            assertSame(bw1, factory.getResult());
            
            factory.setSimpleMapWrapper(true);
            BeansWrapper bw2 = factory.getResult();
            checkClassIntrospectorCacheSize(1);
            assertNotSame(bw1, bw2);
            assertNotSame(bw1.getClassIntrospector(), bw2.getClassIntrospector());
            assertTrue(bw1.isClassIntrospectionCacheRestricted());
            assertTrue(bw2.isClassIntrospectionCacheRestricted());
            
            // Wrapping tests:
            assertFalse(exposesFields(bw1));
            assertFalse(exposesFields(bw2));
            assertTrue(exposesProperties(bw1));
            assertTrue(exposesProperties(bw2));
            assertTrue(exposesMethods(bw1));
            assertTrue(exposesMethods(bw2));
            assertFalse(exposesUnsafe(bw1));
            assertFalse(exposesUnsafe(bw2));
            assertFalse(isSimpleMapWrapper(bw1));
            assertTrue(isSimpleMapWrapper(bw2));
        }

        {
            factory = new BeansWrapperBuilder(V_2_3_19);
            factory.setMethodAppearanceFineTuner(GetlessMethodsAsPropertyGettersRule.INSTANCE);  // doesn't spoils sharing
            
            BeansWrapper bw1 = factory.getResult();
            assertSame(bw1, factory.getResult());
            checkClassIntrospectorCacheSize(2);
            
            factory.setSimpleMapWrapper(true);
            BeansWrapper bw2 = factory.getResult();
            checkClassIntrospectorCacheSize(2);
            
            assertNotSame(bw1, bw2);
            assertSame(bw1.getClassIntrospector(), bw2.getClassIntrospector());  // !
            assertTrue(bw1.isClassIntrospectionCacheRestricted());
            assertTrue(bw2.isClassIntrospectionCacheRestricted());
            
            // Wrapping tests:
            assertFalse(exposesFields(bw1));
            assertFalse(exposesFields(bw2));
            assertTrue(exposesProperties(bw1));
            assertTrue(exposesProperties(bw2));
            assertTrue(exposesMethods(bw1));
            assertTrue(exposesMethods(bw2));
            assertFalse(exposesUnsafe(bw1));
            assertFalse(exposesUnsafe(bw2));
            assertFalse(isSimpleMapWrapper(bw1));
            assertTrue(isSimpleMapWrapper(bw2));
        }
        
        assertTrue(hardReferences.size() != 0);  // just to save it from GC until this line        
    }
    
    private void checkClassIntrospectorCacheSize(int expectedSize) {
        assertEquals(expectedSize, getClassIntrospectorInstanceCacheSize());
    }

    private void assertNotEquals(Object o1, Object o2) {
        assertFalse(o1.equals(o2));
    }
    
    public class C {
        
        public String foo = "FOO";
        
        public String getBar() {
            return "BAR";
        }
        
    }

    private boolean isSimpleMapWrapper(BeansWrapper bw) throws TemplateModelException {
        return bw.wrap(new HashMap()) instanceof SimpleMapModel;
    }
    
    private boolean exposesFields(BeansWrapper bw) throws TemplateModelException {
        TemplateHashModel thm = (TemplateHashModel) bw.wrap(new C());
        TemplateScalarModel r = (TemplateScalarModel) thm.get("foo");
        if (r == null) return false;
        assertEquals("FOO", r.getAsString());
        return true;
    }

    private boolean exposesProperties(BeansWrapper bw) throws TemplateModelException {
        TemplateHashModel thm = (TemplateHashModel) bw.wrap(new C());
        TemplateScalarModel r = (TemplateScalarModel) thm.get("bar");
        if (r == null) return false;
        assertEquals("BAR", r.getAsString());
        return true;
    }

    private boolean exposesMethods(BeansWrapper bw) throws TemplateModelException {
        TemplateHashModel thm = (TemplateHashModel) bw.wrap(new C());
        return thm.get("getBar") != null;
    }

    private boolean exposesUnsafe(BeansWrapper bw) throws TemplateModelException {
        TemplateHashModel thm = (TemplateHashModel) bw.wrap(new C());
        return thm.get("wait") != null;
    }
    
    static int getClassIntrospectorInstanceCacheSize() {
        Map instanceCache = ClassIntrospectorBuilder.getInstanceCache();
        synchronized (instanceCache) {
            return instanceCache.size();
        }
    }

    static int getClassIntrospectorNonClearedInstanceCacheSize() {
        Map instanceCache = ClassIntrospectorBuilder.getInstanceCache();
        synchronized (instanceCache) {
            int cnt = 0;
            for (Iterator it = instanceCache.values().iterator(); it.hasNext(); ) {
                if (((Reference) it.next()).get() != null) cnt++;
            }
            return cnt;
        }
    }
    
    static void clearClassIntrospectorInstanceCacheReferences(boolean enqueue) {
        Map instanceCache = ClassIntrospectorBuilder.getInstanceCache();
        synchronized (instanceCache) {
            for (Iterator it = instanceCache.values().iterator(); it.hasNext(); ) {
                Reference ref = ((Reference) it.next());
                ref.clear();
                if (enqueue) {
                    ref.enqueue();
                }
            }
        }
    }

    static int getBeansWrapperInstanceCacheSize() {
        Map instanceCache = BeansWrapperBuilder.getInstanceCache();
        synchronized (instanceCache) {
            int size = 0; 
            for (Iterator it1 = instanceCache.values().iterator(); it1.hasNext(); ) {
                size += ((Map) it1.next()).size();
            }
            return size;
        }
    }

    static int getBeansWrapperNonClearedInstanceCacheSize() {
        Map instanceCache = BeansWrapperBuilder.getInstanceCache();
        synchronized (instanceCache) {
            int cnt = 0;
            for (Iterator it1 = instanceCache.values().iterator(); it1.hasNext(); ) {
                Map tcclScope = (Map) it1.next();
                for (Iterator it2 = tcclScope.values().iterator(); it2.hasNext(); ) {
                    if (((Reference) it2.next()).get() != null) cnt++;
                }
            }
            return cnt;
        }
    }
    
    static void clearBeansWrapperInstanceCacheReferences(boolean enqueue) {
        Map instanceCache = BeansWrapperBuilder.getInstanceCache();
        synchronized (instanceCache) {
            for (Iterator it1 = instanceCache.values().iterator(); it1.hasNext(); ) {
                Map tcclScope = (Map) it1.next();
                for (Iterator it2 = tcclScope.values().iterator(); it2.hasNext(); ) {
                    Reference ref = ((Reference) it2.next());
                    ref.clear();
                    if (enqueue) {
                        ref.enqueue();
                    }
                }
            }
        }
    }
    
}