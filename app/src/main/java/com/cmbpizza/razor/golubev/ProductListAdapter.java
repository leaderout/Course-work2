package com.cmbpizza.razor.golubev;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class ProductListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private ArrayList<Products> productList;

    ProductListAdapter(Context context, int layout, ArrayList<Products> productList) {
        this.context = context;
        this.layout = layout;
        this.productList = productList;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int itemPosition) {
        return productList.get(itemPosition);
    }

    @Override
    public long getItemId(int itemPosition) {
        return itemPosition;
    }

    private class ViewHolder{
        ImageView productImage;
        TextView productId, productTitle, productCategory, productPrice, productDescription;
    }

    @Override
    public View getView(int itemPosition, View view, ViewGroup viewGroup) {
        View row = view;
        ViewHolder viewHolder = new ViewHolder();

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout, null);

            row.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) row.getTag();
        }

        viewHolder.productId = row.findViewById(R.id.productId);
        viewHolder.productTitle = row.findViewById(R.id.productTitle);
        viewHolder.productCategory = row.findViewById(R.id.productCategory);
        viewHolder.productPrice = row.findViewById(R.id.productPrice);
        viewHolder.productDescription = row.findViewById(R.id.productDescription);
        viewHolder.productImage = row.findViewById(R.id.productImage);

        Products product = productList.get(itemPosition);

        viewHolder.productId.setText(String.valueOf(product.getProductId()));
        viewHolder.productTitle.setText(product.getProductTitle());
        viewHolder.productCategory.setText(product.getProductCategory());
        viewHolder.productPrice.setText(context.getResources().getString(R.string.txtProductPricePrefix, product.getProductPrice()));
        viewHolder.productDescription.setText(product.getProductDescription());

        byte[] productImageByte = product.getProductImage();
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
        viewHolder.productImage.setImageBitmap(imageBitmap);

        return row;
    }
}
