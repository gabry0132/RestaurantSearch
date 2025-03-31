package com.example.restaurantsearch;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

//RecyclerViewのAdapterに関する設定と処理は全部ここに書きました。完全に新しい作り方になっていますので勉強しながら作ったため、完璧ではないはずです。
public class ShopsRecyclerViewAdapter extends RecyclerView.Adapter<ShopsRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Shop> shops = new ArrayList<>();
    private Context context;

    public ShopsRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    //戻り値がViewHolder型。以前に作ったViewHolderクラスの代入に使います。
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate関数のパラメータ：代入しようとしているviewオブジェクトを自分のViewGroup (parent)とつなげます。それで行いますのでさらにattachToRootする必要がないので最後のパラメータがfalse
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shops_list_item, parent, false);  //どこのviewになるのか分からなければ、ViewGroupをnullにして、attachToRootをtrueにすれば良い
        //実際にViewHolderオブジェクトを戻します。
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //子クラスが本クラス内にありますので直接フィールドへアクセスできます。
        holder.txtShopName.setText(shops.get(position).getName());
        holder.txtDistance.setText(shops.get(position).getDistanceFromSearchCoordinates() + "m");
        holder.txtGenre.setText(shops.get(position).getGenre());
        //txtGenreがwidth="wrap-contents"であっても、空文字の場合は右のマージンが表示されるのでgenreがない時に次のtxtEstimatedPriceが右に移動してしまいますので、マージンがいらない場合は削除します。
        if(holder.txtGenre.getText().toString().isEmpty()) removeRightMargin(holder.txtGenre);
        //genreがある時に問題なくgenreとestimatedPriceの隙間が表示されますが、下の方へスクロールすると、戻ったら隙間がなくなったことが確認できます。onScrollでの　表示処理のコードの問題ですね。アンドロイドの問題ですので修正しようと思いません。
        holder.txtEstimatedPrice.setText(shops.get(position).getEstimatedPrice());
        holder.txtAccess.setText(shops.get(position).getAccess());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ロード中だったら何もさせません
                if(RestaurantsListActivity.currentlyLoading) return;

                //普通は一覧からIDを取得して、詳細画面に入ったらそのIDを使って詳細データを取得するんですが、今回は一覧の結果にはお店の全てのデータが入っていますので2回APIを呼ぶより対象のオブジェクトを次の画面に渡したほうが速いです。
                //オブジェクトを次のアクティビティに渡すために、Shopクラスは implements Serializableにしました。そうすると自動的にデータがバイトの形で送信できるようになります。
                //Serializableと同じ機能のParcelableクラスもありそうですがスピードよりShopクラスの分かりやすさの方が優先したかったので@OverrideメソッドがないSerializableにしました。
                Intent intent = new Intent(context, RestaurantDetailsActivity.class);
                //onClick()の中でpositionがアクセスできないのでgetAdapterPosition()でindexを求めて、レストラン詳細ページへ移動させます。
                intent.putExtra("shop", shops.get(holder.getAdapterPosition()));
                ContextCompat.startActivity(context, intent,null);
            }
        });

        //Glideで画像管理を行います。最も基本的なGlideの設定でいいとします。
        Glide.with(context)
                .asBitmap()
                .load(shops.get(position).getImageUrl())
                .into(holder.image);
    }

    private void removeRightMargin(View view) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMarginEnd(0);
        view.requestLayout();
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    public void setShops(ArrayList<Shop> shops) {
        this.shops = shops;
        //データセットが更新されたといってもRecyclerViewに通知する必要があるようです。
        notifyDataSetChanged();
    }

    //参考動画：https://www.youtube.com/watch?v=fis26HvvDII 9時間20分から
    public class ViewHolder extends RecyclerView.ViewHolder{

        //ここで作成する変数は親クラスの「onBindViewHolder」に使います。
        private CardView parent;
        private TextView txtShopName, txtDistance, txtGenre, txtEstimatedPrice, txtAccess;
        private ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtDistance = itemView.findViewById(R.id.txtDistance);
            txtGenre = itemView.findViewById(R.id.txtGenre);
            txtEstimatedPrice = itemView.findViewById(R.id.txtEstimatedPrice);
            txtAccess = itemView.findViewById(R.id.txtAccess);
            image = itemView.findViewById(R.id.image);
        }
    }
}
