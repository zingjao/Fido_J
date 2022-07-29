package com.example.fido_j;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CredentialsAdapter extends RecyclerView.Adapter<CredentialsAdapter.MyHolder> {
    ArrayList<String> data1,data2;//宣告資料1(title)、資料2(body)
    public CredentialsAdapter(ArrayList<String> data1,ArrayList<String> data2){
        //取得List內容
        this.data1=data1;
        this.data2=data2;
    }
    //將原本RecyclerView.ViewHolder的部分皆改為MyHolder
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.credentials_item,parent,false);
        return new MyHolder(view);//連接布局，新增一個view給viewholder綁定元件
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        holder.title.setText(data1.get(position));//position為索引值，用get來取得arraylist資料
        holder.body.setText(data2.get(position));
        holder.delete.setOnClickListener(view -> {
            data1.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return data1.size();    //回傳List大小
    }
    public class MyHolder extends RecyclerView.ViewHolder{
        private TextView title,body;
        private ImageButton delete;
        public MyHolder(View Holder){
            super(Holder);
            //取得從onCreateViewHolder的view，此ViewHolder綁定主布局元件
            title=Holder.findViewById(R.id.id);
            body=Holder.findViewById(R.id.public_key);
            delete=Holder.findViewById(R.id.delete);
        }
    }
}