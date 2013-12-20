package org.nextcoin.transactions;

import org.nextcoin.accounts.Account;
import org.nextcoin.accounts.AccountsManager;
import org.nextcoin.nxtclient.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TransactionsActivity extends Activity {
    
    static public void open(Context context, int accountPos){
        Intent intent = new Intent(context, TransactionsActivity.class);
        intent.putExtra("AccountPos", accountPos);
        context.startActivity(intent);
    }

    private Account mAccount;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transactions_activity);
        int accountPos = this.getIntent().getIntExtra("AccountPos", 0);
        mAccount = AccountsManager.sharedInstance().getAccountList().get(accountPos);
        
        TextView textViewAccountId = (TextView)this.findViewById(R.id.textview_account_id);
        textViewAccountId.setText(mAccount.mId);
        
        TextView textViewAccountBalance = (TextView)this.findViewById(R.id.textview_account_balance);
        textViewAccountBalance.setText("Balance:  " + mAccount.getBalanceText());
        
        Button btnSend = (Button)this.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendCoinsActivity.open(TransactionsActivity.this, mAccount.mId, "");
            }
        });
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
