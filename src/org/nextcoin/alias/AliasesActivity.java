package org.nextcoin.alias;

import org.nextcoin.accounts.Account;
import org.nextcoin.accounts.AccountsInfoHelper;
import org.nextcoin.accounts.AccountsManager;
import org.nextcoin.nxtclient.R;
import org.nextcoin.transactions.SendCoinsActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AliasesActivity extends Activity {

    private AccountsInfoHelper.ResponseListener mResponseListener = 
            new AccountsInfoHelper.ResponseListener() {
        @Override
        public void onResponse(boolean success, Account account, String info) {
            if ( success ){
                mAliasListView.setAliasList(account.mAliasList);
                mAliasListView.notifyDataSetChanged();
            }
        }
    };
    
    static public void open(Context context, int accountPos){
        Intent intent = new Intent(context, AliasesActivity.class);
        intent.putExtra("AccountPos", accountPos);
        context.startActivity(intent);
    }

    private Account mAccount;
    private AliasListView mAliasListView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliases_activity);
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
                SendCoinsActivity.open(AliasesActivity.this, mAccount.mId, "");
            }
        });
        
        mAliasListView = 
                (AliasListView)this.findViewById(R.id.listview_alias);
        mAliasListView.setAccount(mAccount);
        new AccountsInfoHelper().requestAliasList(mAccount, mResponseListener);
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
