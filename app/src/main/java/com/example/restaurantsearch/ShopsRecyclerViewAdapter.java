package com.example.restaurantsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
        holder.txtGenre.setText(shops.get(position).getGenre());
        holder.txtEstimatedPrice.setText(shops.get(position).getEstimatedPrice());
        holder.txtAccess.setText(shops.get(position).getAccess());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ロード中だったら何もさせません
                if(RestaurantsListActivity.currentlyLoading) return;

                //onClick()の中でpositionがアクセスできないのでgetAdapterPosition()でindexを求めます。
                Toast.makeText(context, "selected " + shops.get(holder.getAdapterPosition()).getName(), Toast.LENGTH_SHORT).show();
            }
        });

        //Glideで画像管理を行います。最も基本的なGlideの設定でいいとします。
        Glide.with(context)
                .asBitmap()
                .load(shops.get(position).getImageUrl())
                .into(holder.image);
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
        private TextView txtShopName, txtGenre, txtEstimatedPrice, txtAccess;
        private ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtGenre = itemView.findViewById(R.id.txtGenre);
            txtEstimatedPrice = itemView.findViewById(R.id.txtEstimatedPrice);
            txtAccess = itemView.findViewById(R.id.txtAccess);
            image = itemView.findViewById(R.id.image);
        }
    }
}
