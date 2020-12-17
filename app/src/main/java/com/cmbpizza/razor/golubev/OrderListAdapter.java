package com.cmbpizza.razor.golubev;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class OrderListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private SQLLiteHelperProducts sqlLiteHelperProducts;

    private ArrayList<Orders> orders;

    OrderListAdapter(Context context, int layout, ArrayList<Orders> orders) {
        this.context = context;
        this.layout = layout;
        this.orders = orders;
        this.sqlLiteHelperProducts = new SQLLiteHelperProducts(context, "ProductDB", null, 1);
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int itemPosition) {
        return orders.get(itemPosition);
    }

    @Override
    public long getItemId(int itemPosition) {
        return itemPosition;
    }

    private class ViewHolder{
        TextView orderNetTotalPrice, orderTitlesWithQuantity, orderIdCheck;
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

        viewHolder.orderNetTotalPrice = row.findViewById(R.id.orderPriceNetTotalCheck);
        viewHolder.orderTitlesWithQuantity = row.findViewById(R.id.orderProductTitlesWithQuantity);
        viewHolder.orderIdCheck = row.findViewById(R.id.orderIdCheck);

        final Orders orderItems = orders.get(itemPosition);

        final String orderId = String.valueOf(orderItems.getOrderId());
        final String[] productIdList = orderItems.getProductIdList();
        final String[] productQuantityList = orderItems.getProductQuantityList();
        int productNetTotalPrice = orderItems.getNetTotalPrice();

        String titleWithQuantity = "";

        for(int i = 0; i < productIdList.length; i++){
            int productId = Integer.valueOf(productIdList[i]);
            int productQuantity = Integer.valueOf(productQuantityList[i]);
            String productTitle = sqlLiteHelperProducts.getProductTitle(productId);
            StringBuilder builder = new StringBuilder(titleWithQuantity);
            builder.append(productTitle);
            builder.append(": ");
            builder.append(productQuantity);
            if(i != productIdList.length - 1){
                builder.append(", ");
            }
            titleWithQuantity = builder.toString();
        }

        viewHolder.orderIdCheck.setText(orderId);
        viewHolder.orderNetTotalPrice.setText(context.getResources().getString(R.string.txtProductPricePrefix, productNetTotalPrice));
        viewHolder.orderTitlesWithQuantity.setText(titleWithQuantity);

        ImageButton ConfirmOrderButton = row.findViewById(R.id.btnConfirmOrder);
        ConfirmOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sqlLiteHelperProducts.confirmOrderStatus(orderId);
                Toast.makeText(context, "Заказ " + orderId + " подтверждён!", Toast.LENGTH_SHORT).show();
                orders.remove(itemPosition);
                notifyDataSetChanged();
            }
        });


        return row;
    }
}
