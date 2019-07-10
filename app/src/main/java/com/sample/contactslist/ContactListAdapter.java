package com.sample.contactslist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sample.contactslist.Pojo.ContactModel;

import java.io.File;
import java.util.List;
import java.util.Random;

import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.MyViewHolder> {
    Context context;
    List<ContactModel> getContactInfo;

    public ContactListAdapter(Context xcontext, List<ContactModel> xgetContactInfo) {
        this.context = xcontext;
        this.getContactInfo = xgetContactInfo;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        try {
            Typeface fontType = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
            ContactModel contactInfo = getContactInfo.get(position);
            holder.contact_name.setText(contactInfo.name);
            holder.contact_name.setTypeface(fontType);
            if (contactInfo.photoURI != null) {

                File imgFile = new File(contactInfo.photoURI.getPath());
                if (imgFile.exists()) {
                    holder.contact_img.setImageURI(contactInfo.photoURI);
                    holder.contact_img.setVisibility(View.VISIBLE);
                } else {
                    holder.contact_img.setVisibility(View.GONE);
                    String firstname = String.valueOf(contactInfo.name.charAt(0));
                    holder.flname_txt.setText(firstname);
                    holder.flname_txt.setTypeface(fontType);
                    holder.flname_txt.setVisibility(View.VISIBLE);
                }

                GradientDrawable drawable = (GradientDrawable) holder.imageview_lyout.getBackground();
                Random rand = new Random();
                drawable.setColor(Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
            }

            holder.imageview_lyout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri u = Uri.parse("tel:" + contactInfo.mobileNumber);
                    Intent i = new Intent(Intent.ACTION_DIAL, u);
                    try {
                        context.startActivity(i);
                    } catch (SecurityException s) {
                        s.printStackTrace();
                    }
                }
            });

            holder.contact_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AddContact.class);
                    intent.putExtra("UPDATE", "UPDATE");
                    intent.putExtra("NAME", contactInfo.name);
                    intent.putExtra("NUMBER", contactInfo.mobileNumber);
                    context.startActivity(intent);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return getContactInfo.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.contact_name)
        TextView contact_name;
        @BindView(R.id.imageview_lyout)
        LinearLayout imageview_lyout;
        @BindView(R.id.flname_txt)
        TextView flname_txt;
        @BindView(R.id.contact_img)
        ImageView contact_img;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
