package io.goshin.bukadarkness.adapter;


import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import de.daslaboratorium.machinelearning.classifier.Classification;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import io.goshin.bukadarkness.Hook;
import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.database.ChapterMapDatabase;
import io.goshin.bukadarkness.database.ChapterUpdateTimeDatabase;
import io.goshin.bukadarkness.database.CoverMapDatabase;
import io.goshin.bukadarkness.database.ImageReferrerMapDatabase;
import io.goshin.bukadarkness.database.MangaMapDatabase;
import io.goshin.bukadarkness.sited.Client;

public class Detail extends MangaAdapter {
    private static Classifier<String, String> classifier;

    private static Classifier<String, String> getClassifier() {
        if (classifier == null) {
            classifier = new BayesClassifier<>();

            String[] seriesChapter = new String[]{"話", "话", "第話", "第话", "第回", "第"};
            String[] offprintChapter = new String[]{"卷", "册", "第卷", "第册"};
            String[] extraChapter = new String[]{"设定", "采访", "日常", "同人", "人气投票", "下编", "中篇", "前编", "外传下", "外传上", "番外篇", "第卷番外", "番外篇", "番外篇", "番外篇", "附录漫画", "番外", "番外", "篇", "番外", "外传", "番外", "番外", "设定", "第卷番外", "特别篇", "番外篇", "小剧场", "番外", "附赠", "彩页", "番外篇", "番外篇", "番外篇", "番外", "番外篇", "番外篇", "番外篇", "四格", "番外篇", "第卷番外", "第卷番外", "特别篇", "番外篇", "附录", "番外篇", "番外篇", "动画官网彩页(附无字版)", "琦玉老师与恐龙(推图P)", "MJ番外篇", "可爱的野猫", "弹丸天使", "番外", "YougJump出张版", "YougJump出张版", "live出张版", "卷番外", "番外篇", "附刊", "番外篇", "短篇"};

            for (String name : seriesChapter) {
                for (int i = 0; i < 13; i++) {
                    classifier.learn("series", Arrays.asList(name.split("|")));
                }
            }
            for (String name : offprintChapter) {
                for (int i = 0; i < 5; i++) {
                    classifier.learn("offprint", Arrays.asList(name.split("|")));
                }
            }
            for (String name : extraChapter) {
                classifier.learn("extra", Arrays.asList(name.split("|")));
            }
        }
        return classifier;
    }

    @Override
    public Boolean needRedirect(JSONObject params) throws Throwable {
        return !MangaMapDatabase.getInstance().getUrl(params.optString("mid")).equals("");
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public String getResult(JSONObject params, JSONObject originalResult) throws Throwable {
        if (originalResult != null) {
            return originalResult.toString();
        }

        String sourceFilename = MangaMapDatabase.getInstance().getFilename(params.optString("mid"));
        String url = MangaMapDatabase.getInstance().getUrl(params.optString("mid"));
        if (url == null || url.equals("")) {
            return "{ret:-1}";
        }
        params.put("fp", sourceFilename);
        params.put("url", url);

        JSONObject result = new JSONObject("{\n" +
                "    \"ret\": 0,\n" +
                "    \"logo\": \"http://c-r5.sosobook.cn/logo/logo5/217176/201601211756/43m.jpg\",\n" +
                "    \"logos\": \"http://c-r5.sosobook.cn/logo/logo5/217176/201601211756/43m.jpg\",\n" +
                "    \"logodir\": \"http://c-r5.sosobook.cn/logo/logo5/217176/201601211756\",\n" +
                "    \"name\": \"绅士壹周刊\",\n" +
                "    \"author\": \"17173动漫频道\",\n" +
                "    \"rate\": \"97\",\n" +
                "    \"intro\": \"还有一大堆绅士盘点等着你哟！\",\n" +
                "    \"lastup\": \" \",\n" +
                "    \"lastupcid\": \"-1\",\n" +
                "    \"lastuptime\": \"2016-01-21\",\n" +
                "    \"lastuptimeex\": \"2016-01-21 18:01:15\",\n" +
                "    \"resupno\": \"1453370539\",\n" +
                "    \"upno\": \"2211\",\n" +
                "    \"readmode3\": \"0\",\n" +
                "    \"readmode2\": 0,\n" +
                "    \"readmode\": 50331648,\n" +
                "    \"popular\": 0,\n" +
                "    \"populars\": \"D+\",\n" +
                "    \"favor\": 0,\n" +
                "    \"finish\": \"0\",\n" +
                "    \"discount\": \"1\",\n" +
                "    \"fav_exceed\": \"超过 1%\",\n" +
                "    \"read_exceed\": \"超过 17%\",\n" +
                "    \"shareurl\": \"http://m.sosobook.cn/manga.php?page=1\",\n" +
                "    \"detail_shareurl\": \"http://m.buka.cn/detail.php?mid=217176&src=android\",\n" +
                "    \"recomctrltype\": 0,\n" +
                "    \"recomctrlparam\": \"\",\n" +
                "    \"recomwords\": \"\",\n" +
                "    \"recomenter\": \"\",\n" +
                "    \"recomdelay\": 0,\n" +
                "    \"urls\": [\n" +
                "        {\n" +
                "            \"name\": \"投稿须知\",\n" +
                "            \"url\": \"http://soft.bukamanhua.com:8000/contribution.php\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"内容举报\",\n" +
                "            \"url\": \"http://soft.bukamanhua.com:8000/report.php\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"footer\": [\n" +
                "        {\n" +
                "            \"text\": \"投稿须知\",\n" +
                "            \"ctrltype\": 11,\n" +
                "            \"ctrlparam\": \"http://soft.bukamanhua.com:8000/contribution.php,投稿须知\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"text\": \"内容举报\",\n" +
                "            \"ctrltype\": 11,\n" +
                "            \"ctrlparam\": \"http://soft.bukamanhua.com:8000/report.php,内容举报\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"links\": [],\n" +
                "    \"res\": []\n" +
                "}");

        JSONObject response = new JSONObject(Client.request(params));
        String logo = Utils.getEncodedUrl(response.optString("logo"));
        String logoHash = params.optString("mid") + Math.abs(logo.hashCode());
        String fakeCoverDir = Items.COVER_PREFIX + logoHash + "/";

        synchronized (CoverMapDatabase.getInstance()) {
            CoverMapDatabase coverMap = CoverMapDatabase.getInstance();
            coverMap.putPrepare();
            coverMap.put(logoHash, logo);
            coverMap.commit();
        }

        synchronized (ImageReferrerMapDatabase.getInstance()) {
            ImageReferrerMapDatabase imageReferrerMap = ImageReferrerMapDatabase.getInstance();
            imageReferrerMap.putPrepare();
            imageReferrerMap.put(logo, url);
            imageReferrerMap.commit();
        }

        result.put("logo", fakeCoverDir + "1.jpg");
        result.put("logos", fakeCoverDir + "1.jpg");
        result.put("logodir", fakeCoverDir);

        result.put("name", response.optString("name"));
        result.put("author", response.optString("author"));
        result.put("intro", response.optString("intro"));

        JSONArray sections = response.getJSONArray("sections");
        int length = sections.length();
        int seriesCount = 1;
        int offprintCount = 1;
        int extraCount = 1;
        synchronized (ChapterMapDatabase.getInstance()) {
            ChapterMapDatabase chapterMapDatabase = ChapterMapDatabase.getInstance();
            chapterMapDatabase.putPrepare();
            for (int i = 1; i <= length; i++) {
                JSONObject section = sections.getJSONObject(length - i);
                JSONObject link = new JSONObject("{\n" +
                        "    \"cid\": \"65542\",\n" +
                        "    \"idx\": \"6\",\n" +
                        "    \"type\": \"0\",\n" +
                        "    \"title\": \"第六期\",\n" +
                        "    \"size\": \"8888\",\n" +
                        "    \"ressupport\": \"7\"\n" +
                        "}");
                JSONObject res = new JSONObject("{\n" +
                        "    \"cid\": \"65537\",\n" +
                        "    \"restype\": \"1\",\n" +
                        "    \"csize\": \"8888\"\n" +
                        "}");
                link.put("idx", String.valueOf(i));
                String chapterName = section.optString("name").replaceFirst(".*?(\\d.+)$", "$1");
                link.put("title", chapterName);

                int cid;
                switch (Hook.config.getBoolean("classifyChapter") ? getCategory(section.optString("name")) : "series") {
                    default:
                        cid = 0x10000 + seriesCount++;
                        break;
                    case "offprint":
                        cid = 0x20000 + offprintCount++;
                        break;
                    case "extra":
                        cid = 0x30000 + extraCount++;
                        break;
                }
                String chapterID = String.valueOf(cid);
                if (!params.optBoolean("simple")) {
                    chapterMapDatabase.put(
                            Long.parseLong(params.optString("mid")),
                            Long.parseLong(chapterID),
                            params.optString("fp"),
                            section.optString("url")
                    );
                }
                link.put("cid", chapterID);
                link.put("type", cid / 0x10000 - 1);
                res.put("cid", chapterID);

                if (i == length) {
                    result.put("lastup", chapterName);
                    result.put("lastupcid", chapterID);
                }

                result.getJSONArray("links").put(length - i, link);
                result.getJSONArray("res").put(length - i, res);
            }
            chapterMapDatabase.commit();
        }


        String updateTime = response.optString("updateTime");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        if (!updateTime.equals("")) {
            try {
                date = dateFormat.parse(updateTime);
            } catch (Exception ignored) {
            }
        }
        updateTime = ChapterUpdateTimeDatabase.getInstance().getUpdateTime(
                params.optString("mid"), result.optString("lastupcid"), dateFormat.format(date));
        result.put("lastuptime", updateTime);
        result.put("lastuptimeex", updateTime + " 00:00:00");

        return result.toString();
    }

    public String getCategory(String name) {
        name = name.replaceAll("\\s+", "").replaceAll("\\d+", "n");
        Classification<String, String> classification = getClassifier().classify(Arrays.asList(name.split("|")));
        return classification.getCategory();
    }
}
