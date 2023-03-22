package com.example.fasta.shared;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.fasta.R;
import com.example.fasta.model.Model;
import com.example.fasta.model.Recipe;
import com.squareup.picasso.Picasso;

public class CardViewHolder extends RecyclerView.ViewHolder{
    public ImageView itemImage, contactImage;
    public TextView title;
    public TextView date;
    public TextView isSold;
    private  Context context;

    public CardViewHolder(@NonNull View itemView, OnItemClickListener listener, Context context) {
        super(itemView);
        getProductCardElements(itemView);
        this.context = context;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                listener.onItemClick(v, pos);
            }
        });
    }

    private void getProductCardElements(@NonNull View itemView) {
        itemImage = itemView.findViewById(R.id.image_item);
        contactImage = itemView.findViewById(R.id.contact_image);
        title = itemView.findViewById(R.id.item_title_tv);
        date = itemView.findViewById(R.id.upload_product_date_tv);
        isSold = itemView.findViewById(R.id.item_sold);
        contactImage.setImageResource(R.drawable.no_person_image);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void bind(Recipe recipe) {
        title.setText(recipe.getTitle());
        itemImage.setImageResource(R.drawable.no_product_image);
        isSold.setVisibility(View.INVISIBLE);
        date.setText(UtilFunctions.getDate(recipe.getUpdateDate()));

        if (recipe.getImageUrl() != null) {
            Picasso.get()
                    .load(recipe.getImageUrl())
                    .into(itemImage);
        } else {
            itemImage.setImageResource(R.drawable.no_product_image);
        }


        Model.instance.getUser(recipe.getContactId(), user -> {
            if (user.getUserImageUrl() != null) {

                Glide.with(this.context)
                        .load(user.getUserImageUrl())
                        .apply(RequestOptions.circleCropTransform()).into(contactImage);
            }
        });
    }
}
