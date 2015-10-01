package eea.eprtr.cms.util;

import java.util.ArrayList;
import java.util.List;
import eea.eprtr.cms.model.BreadCrumb;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.springframework.ui.ExtendedModelMap;

public class BreadCrumbsTest {

    @Test
    public void nullTest() {
        ExtendedModelMap testModel = new ExtendedModelMap();
        BreadCrumbs.set(testModel);
        assertTrue(testModel.containsAttribute("breadcrumbs"));
        List<BreadCrumb> breadcrumbList = (List<BreadCrumb>) testModel.get("breadcrumbs");
        assertEquals(1, breadcrumbList.size());
    }

    @Test
    public void aboutCrumb() {
        ExtendedModelMap testModel = new ExtendedModelMap();
        BreadCrumbs.set(testModel, new BreadCrumb("/about", "About E-PRTR"));
        assertTrue(testModel.containsAttribute("breadcrumbs"));
        List<BreadCrumb> breadcrumbList = (List<BreadCrumb>) testModel.get("breadcrumbs");
        assertEquals(2, breadcrumbList.size());
    }
}
