package io.goshin.bukadarkness;

import org.junit.Test;

import io.goshin.bukadarkness.adapter.Detail;

import static org.junit.Assert.assertEquals;

public class ClassifierTest {
    @Test
    public void classifyTest() throws Exception {
        assertEquals("extra", new Detail().getCategory("设定集2"));
        assertEquals("extra", new Detail().getCategory("第2回番外"));
        assertEquals("extra", new Detail().getCategory("附赠彩页"));

        assertEquals("series", new Detail().getCategory("2"));
        assertEquals("series", new Detail().getCategory("2集"));
        assertEquals("series", new Detail().getCategory("第2话"));
        assertEquals("series", new Detail().getCategory("第2回"));
        assertEquals("series", new Detail().getCategory("第2集"));
        assertEquals("series", new Detail().getCategory("第2"));

        assertEquals("offprint", new Detail().getCategory("2卷"));
        assertEquals("offprint", new Detail().getCategory("第2卷"));
    }
}
