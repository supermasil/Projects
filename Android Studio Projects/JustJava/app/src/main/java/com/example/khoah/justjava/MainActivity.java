package com.example.khoah.justjava;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.text.NumberFormat;

/**
 * This app displays an order form to order coffee.
 */
public class MainActivity extends AppCompatActivity {
    int quantity = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * This method is called when the order button is clicked.
     */
    public void submitOrder(View view) {
        String priceMessage = "Thank you";
        displayPrice(quantity * 5);
        displayMessage(priceMessage);
    }

    /**
     * This method increases the quantity as clicked
     */
    public void increment (View view) {
        quantity ++;
        display(quantity);
    }

    /**
     * This method decreases the quantity as clicked
     */
    public void decrement (View view) {
        if (quantity != 0) {
            quantity --;
            display(quantity);
        }

    }

    /**
     * This method displays the message when order button is hit
     * */
    public void displayMessage (String message) {
        TextView priceTextView = (TextView) findViewById(R.id.message);
        priceTextView.setText(message);
    }

    /**
     * This method displays the given quantity value on the screen.
     */
    private void display(int number) {
        TextView quantityTextView = (TextView) findViewById(R.id.quantity_text_view);
        quantityTextView.setText("" + number);
    }

    /**
     * This method displays the given price on the screen
     * @param number
     */
    private void displayPrice(int number) {
        TextView priceTextView = (TextView) findViewById(R.id.price_text_view);
        priceTextView.setText(NumberFormat.getCurrencyInstance().format(number));
    }
}