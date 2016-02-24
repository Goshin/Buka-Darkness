package io.goshin.bukadarkness.sited;

import android.app.Application;

import org.noear.sited.ISdViewModel;
import org.noear.sited.SdNode;
import org.noear.sited.SdNodeSet;
import org.noear.sited.SdSource;
import org.noear.sited.SdSourceCallback;

public class MangaSource extends SdSource {

    private SdNodeSet home;
    private SdNodeSet main;

    private String intro;
    private String author;
    private int type;

    public MangaSource(Application app, String xml) throws Exception {
        super(app, xml);
        main = getBody("main");
        home = (SdNodeSet) main.get("home");

        author = attrs.getString("author");
        intro = attrs.getString("intro");
        type = main.attrs.getInt("dtype");
    }

    public interface Callback {
        void run(Object... objects);
    }

    public int getType() {
        return type;
    }


    public String getAuthor() {
        return author;
    }

    public String getIntro() {
        return intro;
    }

    public void getHots(final Callback callback) {
        SdNode hots = (SdNode) home.get("hots");
        final DataModel hotsViewModel = new DataModel();
        getNodeViewModel(hotsViewModel, false, 1, hots, new SdSourceCallback() {
            @Override
            public void run(Integer code) {
                callback.run(hotsViewModel.getJsonData(), MangaSource.this);
            }
        });
    }

    public void search(String keyword, final Callback callback) {
        SdNode search = (SdNode) main.get("search");
        final DataModel searchViewModel = new DataModel();
        getNodeViewModel(searchViewModel, false, keyword, 1, search, new SdSourceCallback() {
            @Override
            public void run(Integer code) {
                callback.run(searchViewModel.getJsonData(), MangaSource.this);
            }
        });
    }

    public void getBookDetail(String url, final Callback callback) {
        SdNode book = (SdNode) main.get("book");
        final DataModel dataModel = new DataModel();
        getNodeViewModel(dataModel, false, url, book, new SdSourceCallback() {
            @Override
            public void run(Integer code) {
                callback.run(dataModel.getJsonData());
            }
        });
    }

    public void getSection(String url, final Callback callback) {
        SdNode section = (SdNode) main.get("section");
        final DataModel dataModel = new DataModel();
        getNodeViewModel(dataModel, false, url, section, new SdSourceCallback() {
            @Override
            public void run(Integer code) {
                callback.run(dataModel.getJsonData());
            }
        });
    }

    private class DataModel implements ISdViewModel {
        public String jsonData;

        @Override
        public void loadByConfig(SdNode config) {
        }

        @Override
        public void loadByJson(SdNode config, String... json) {
            jsonData = json[0];
        }

        public String getJsonData() {
            return jsonData;
        }
    }
}
