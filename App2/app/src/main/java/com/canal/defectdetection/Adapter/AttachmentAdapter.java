package com.canal.defectdetection.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.canal.defectdetection.R;

import java.util.List;


public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolderImages> {
    private List<String> imageList;
    private Context context;

    class ViewHolderImages extends RecyclerView.ViewHolder {
        ImageView thumbImage, closeImage;

        ViewHolderImages(View itemView) {
            super(itemView);
            this.thumbImage = (ImageView) itemView.findViewById(R.id.thumbImage);
            this.closeImage = (ImageView) itemView.findViewById(R.id.closeImage);

        }
    }


    public AttachmentAdapter(Context context, List<String> categoriesModels) {
        this.context = context;
        this.imageList = categoriesModels;
    }


    public ViewHolderImages onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderImages(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item, parent, false));
    }

    public void onBindViewHolder(final ViewHolderImages holder, final int position) {
        Glide.with(context).load(imageList.get(position)).into(holder.thumbImage);
        holder.closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageList.remove(position);
                notifyDataSetChanged();

            }
        });

    }


    public int getItemCount() {
        return imageList.size();
    }


}
