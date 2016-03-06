package io.goshin.bukadarkness;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.noear.sited.SdApi;
import org.noear.sited.SdLogListener;
import org.noear.sited.SdNodeFactory;
import org.noear.sited.SdSource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import io.goshin.bukadarkness.database.SourceDatabase;
import io.goshin.bukadarkness.sited.MangaSource;

public class SourceListFragment extends Fragment {


    private View view;
    private FragmentActivity activity;
    private SourceCardListAdapter sourceCardListAdapter;

    public SourceListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_source_list, container, false);
        activity = getActivity();

        SdApi.tryInit(new SdNodeFactory(), new SdLogListener() {
            @Override
            public void run(SdSource source, String tag, String msg, Throwable tr) {
            }
        });
        tryInstallSource(activity.getIntent());
        setUpRecyclerView();

        return view;
    }

    private void setUpRecyclerView() {
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        final Handler handler = new Handler();
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(activity.getString(R.string.loading));
        progressDialog.setMessage(activity.getString(R.string.loading_source));
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                final ArrayList<HashMap<String, String>> list = new ArrayList<>();
                String[] fileList = activity.fileList();
                for (String filename : fileList) {
                    try {
                        FileInputStream fileInputStream = activity.openFileInput(filename);
                        byte[] buffer = new byte[fileInputStream.available()];
                        if (fileInputStream.read(buffer) == -1) {
                            continue;
                        }
                        String xml = new String(buffer);
                        fileInputStream.close();

                        MangaSource mangaSource = new MangaSource(activity.getApplication(), xml);
                        HashMap<String, String> map = new HashMap<>();
                        map.put("filename", filename);
                        map.put("title", mangaSource.title);
                        map.put("author", mangaSource.getAuthor());
                        map.put("intro", mangaSource.getIntro());

                        list.add(map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sourceCardListAdapter = new SourceCardListAdapter(list);
                        sourceCardListAdapter.setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemLongClick(final View view, final int position) {
                                final android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
                                alertDialog
                                        .setMessage(activity.getString(R.string.delete_source))
                                        .setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setPositiveButton(activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String filename = ((TextView) view.findViewById(R.id.textViewCardFilename)).getText().toString();
                                                activity.deleteFile(filename);
                                                new SourceDatabase(activity).delete(filename);
                                                sourceCardListAdapter.removeData(position);
                                            }
                                        })
                                        .setTitle("")
                                        .show();
                            }
                        });
                        recyclerView.setAdapter(sourceCardListAdapter);
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    public void tryInstallSource(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null || !uri.getScheme().equals("sited")) {
            return;
        }
        final String url = new String(Base64.decode(uri.toString().substring(uri.toString().lastIndexOf("/") + 1), Base64.DEFAULT));
        final Callback onError = new Callback() {
            @Override
            public void run(Object... args) {
                String error = (String) args[0];
                Snackbar.make(view.findViewById(R.id.recyclerView),
                        activity.getString(R.string.add_source_failed) + " " + error, Snackbar.LENGTH_LONG).show();
            }
        };
        downloadXml(url, new Callback() {
            @Override
            public void run(Object... args) {
                String xml = (String) args[0];
                try {
                    MangaSource mangaSource = new MangaSource(activity.getApplication(), xml);
                    if (mangaSource.getType() != 1) {
                        onError.run(activity.getString(R.string.source_type_error));
                        return;
                    }
                    String filename = url.substring(url.lastIndexOf("/") + 1);
                    writeToFile(filename, xml);
                    new SourceDatabase(activity).add(filename);
                    Snackbar.make(view.findViewById(R.id.recyclerView),
                            R.string.add_source_success, Snackbar.LENGTH_LONG).show();

                    HashMap<String, String> map = new HashMap<>();
                    map.put("filename", filename);
                    map.put("title", mangaSource.title);
                    map.put("author", mangaSource.getAuthor());
                    map.put("intro", mangaSource.getIntro());
                    sourceCardListAdapter.addData(0, map);
                } catch (Exception e) {
                    onError.run(e.toString());
                    e.printStackTrace();
                }
            }
        }, onError);
    }

    private interface Callback {
        void run(Object... args);
    }

    private void downloadXml(String url, final Callback onSuccess, final Callback onError) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(activity.getString(R.string.loading));
        progressDialog.setMessage(activity.getString(R.string.loading_source));
        progressDialog.show();
        new AsyncHttpClient().get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                onSuccess.run(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                onError.run(activity.getString(R.string.network_error));
            }

            /**
             * Fired in all cases when the request is finished, after both success and failure, override to
             * handle in your own code
             */
            @Override
            public void onFinish() {
                progressDialog.dismiss();
            }
        });
    }

    private void writeToFile(String filename, String content) throws Exception {
        FileOutputStream fileOutputStream = activity.openFileOutput(filename, Context.MODE_PRIVATE);
        fileOutputStream.write(content.getBytes());
        fileOutputStream.close();
    }

    interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    class SourceCardListAdapter extends RecyclerView.Adapter<SourceCardListAdapter.CardViewHolder> {
        ArrayList<HashMap<String, String>> list;

        public SourceCardListAdapter(ArrayList<HashMap<String, String>> data) {
            list = data;
        }

        private OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public void addData(int position, HashMap<String, String> map) {
            list.add(position, map);
            notifyItemInserted(position);
        }

        public void removeData(int position) {
            list.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewtype) {
            return new CardViewHolder(LayoutInflater.from(getActivity()).inflate(
                    R.layout.source_card, parent,
                    false));
        }

        @Override
        public void onBindViewHolder(final CardViewHolder holder, final int position) {
            HashMap<String, String> map = list.get(position);
            holder.filename.setText(map.get("filename"));
            holder.title.setText(map.get("title"));
            holder.author.setText(activity.getString(R.string.author, map.get("author")));
            holder.intro.setText(map.get("intro"));

            holder.card.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.cardview_light_background));
            holder.title.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
            holder.author.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
            holder.intro.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));

            if (onItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = holder.getLayoutPosition();
                        onItemClickListener.onItemClick(holder.itemView, pos);
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int pos = holder.getLayoutPosition();
                        onItemClickListener.onItemLongClick(holder.itemView, pos);
                        return false;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class CardViewHolder extends RecyclerView.ViewHolder {
            private TextView filename;
            private TextView title;
            private TextView author;
            private TextView intro;

            private CardView card;

            public CardViewHolder(View view) {
                super(view);
                card = (CardView) view;
                filename = (TextView) view.findViewById(R.id.textViewCardFilename);
                title = (TextView) view.findViewById(R.id.textViewCardTitle);
                author = (TextView) view.findViewById(R.id.textViewCardAuthor);
                intro = (TextView) view.findViewById(R.id.textViewCardIntro);
            }
        }
    }
}
