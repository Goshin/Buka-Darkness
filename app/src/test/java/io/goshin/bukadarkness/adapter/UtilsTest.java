package io.goshin.bukadarkness.adapter;

import junit.framework.Assert;

import org.junit.Test;

public class UtilsTest {

    @Test
    public void testGetEncodedUrl() throws Exception {
        Assert.assertEquals("http://idx2.seemh.com:88/ps4/z/zjglllgc_fbsx/%E7%AC%AC01%E5%9B%9E/a%20(1).jpg",
                Utils.getEncodedUrl("http://idx2.seemh.com:88/ps4/z/zjglllgc_fbsx/第01回/a (1).jpg"));
    }

    @Test
    public void testStripEmoji() throws Exception {
        Assert.assertEquals("", "\uD83C\uDF1D".replaceAll("[^\\u0000-\\uFFFF]", ""));
    }
}