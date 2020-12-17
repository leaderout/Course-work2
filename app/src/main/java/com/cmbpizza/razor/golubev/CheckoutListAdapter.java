package com.cmbpizza.razor.golubev;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CheckoutListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private SQLLiteHelperProducts sqlLiteHelperProducts;

    private ArrayList<CartItems> cartItems;
    
    CheckoutListAdapter(Context context, int layout, ArrayList<CartItems> cartItems) {

        this.context = context;
        this.layout = layout;
        this.cartItems = cartItems;
        this.sqlLiteHelperProducts = new SQLLiteHelperProducts(context, "ProductDB", null, 1);
    }

    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public Object getItem(int itemPosition) {
        return cartItems.get(itemPosition);
    }

    @Override
    public long getItemId(int itemPosition) {
        return itemPosition;
    }

    private class ViewHolder{
        ImageView productImage;
        TextView productId, productTitle, productTotal, productTotalCalculation, productNetTotal;
    }

    @Override
    public View getView(final int itemPosition, View view, ViewGroup viewGroup) {
        View row = view;
        ViewHolder viewHolder = new ViewHolder();

        if(row == null){

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            row = inflater.inflate(layout, null);

            row.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) row.getTag();
        }

        viewHolder.productId = row.findViewById(R.id.productIdCheckout);
        viewHolder.productTitle = row.findViewById(R.id.productTitleCheckout);
        viewHolder.productTotal = row.findViewById(R.id.productPriceTotalCheckout);
        viewHolder.productTotalCalculation = row.findViewById(R.id.productQuantityCalculationCheckout);
        viewHolder.productImage = row.findViewById(R.id.productImageCheckout);


        final CartItems items = cartItems.get(itemPosition);

        final String cartId = String.valueOf(items.getCartId());
        final int productId = items.getProductId();
        int productQuantity = items.getProductQuantity();
        int productPrice = sqlLiteHelperProducts.getProductPrice(productId);
        int totalPrice = productPrice * productQuantity;
        final int rowId = sqlLiteHelperProducts.getCartRowID(productId);

        String productTitle = sqlLiteHelperProducts.getProductTitle(productId);

        viewHolder.productId.setText(String.valueOf(productId));
        viewHolder.productTitle.setText(productTitle);
        viewHolder.productTotal.setText(context.getResources().getString(R.string.txtProductPricePrefix, totalPrice));
        viewHolder.productTotalCalculation.setText(context.getResources().getString(R.string.txtProductTotalCalculationPrefix, productQuantity, productPrice));


        byte[] productImageByte = sqlLiteHelperProducts.getProductImage(productId);
        if(productImageByte != null){
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
            viewHolder.productImage.setImageBitmap(imageBitmap);
        }

        ImageButton RemoveItemButton = row.findViewById(R.id.btnRemoveCartItem);
        RemoveItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "Item Position: " + String.valueOf(rowId), Toast.LENGTH_SHORT).show();
                cartItems.remove(itemPosition);
                sqlLiteHelperProducts.clearCartItem(rowId, productId);
                notifyDataSetChanged();
                Log.d("cartItem Length", String.valueOf(cartItems.size()));
                Log.d("db Length", String.valueOf(sqlLiteHelperProducts.getCartCount(cartId)));
            }
        });

        return row;
    }
}
