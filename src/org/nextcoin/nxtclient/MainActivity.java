package org.nextcoin.nxtclient;

import org.nextcoin.accounts.AccountsManager;
import org.nextcoin.addresses.AddressesManager;
import org.nextcoin.node.NodeContext;
import org.nextcoin.node.NodesManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private MainFunctionPage mMainFunctionPage;

    /**
     * node information bar
     */
    private TextView mNodeInfoView;
    private Button mChangeNodeButton;
    private NodeContext mNodeContext;
    private View mStateIconView;
    
    private void nodeInfoBarInit(){
        mNodeInfoView = (TextView)this.findViewById(R.id.text_node_info);
        mChangeNodeButton = (Button)this.findViewById(R.id.btn_change_node);
        mStateIconView = (View)this.findViewById(R.id.view_state);
        mChangeNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChangeNodeDialog();
            }
        });
    }
    
    private void nodeInfoBarUpdateResponse(){
        mNodeInfoView.setText(mNodeContext.getIP());
        if ( mNodeContext.isActive() ){
            mStateIconView.setBackgroundResource(R.drawable.icon_online);
            mNodeInfoView.setTextColor(Color.GREEN);
            NodesManager.sharedInstance().changeNodeIP(mNodeContext.getIP());
            NodesManager.sharedInstance().save(this);
        }
        else{
            mStateIconView.setBackgroundResource(R.drawable.icon_offline);
            mNodeInfoView.setTextColor(Color.RED);
        }
    }
    
    private void nodeInfoBarUpdate(){
        mNodeContext.setNodeUpdateListener(new NodeContext.NodeUpdateListener(){
            @Override
            public void onUpdate(NodeContext node) {
                Message msg = new Message();
                msg.what = MSG_NODE_UPDATE;
                msg.obj = MainActivity.this;
                //mMessageHandler.sendMessage(msg);
                mMessageHandler.sendMessageDelayed(msg, 1000);
            }});
        
        mNodeContext.update();
    }
    
    /**
     * change node
     */
    private View mNodeChangeView;
    private AlertDialog mNodeChangeDialog;
    private EditText mEditTextIP;
    private void openChangeNodeDialog(){
        if ( null == mNodeChangeDialog ){
            mNodeChangeView = this.getLayoutInflater().inflate(R.layout.node_input, null);
            mEditTextIP = (EditText)mNodeChangeView.findViewById(R.id.edittext_ip_input);
            mNodeChangeDialog = new AlertDialog.Builder(this)
            .setTitle(R.string.add)
            .setView(mNodeChangeView)
            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    String ipStr = mEditTextIP.getText().toString();
                    if ( ipStr.length() < 7 ){
                        Toast.makeText(MainActivity.this, R.string.ip_format_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    mStateIconView.setBackgroundResource(R.drawable.icon_offline);
                    mNodeInfoView.setTextColor(Color.RED);
                    mNodeContext.setIP(ipStr);
                    nodeInfoBarUpdate();
                }})
            .setNegativeButton(R.string.back, null)
            .create();
        }
        mNodeChangeDialog.show();        
    }

    //
    // message process
    //
    static private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ( msg.obj instanceof MainActivity ){
                MainActivity instance = (MainActivity)msg.obj;
                instance.handleMessage(msg);
            }
        }
    };

    static final private int MSG_NODE_UPDATE = 0;
    public void handleMessage(Message msg) {
        switch (msg.what){
            case MSG_NODE_UPDATE:
                nodeInfoBarUpdateResponse();
                mMainFunctionPage.update();
                break;
            default:
                break;
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AccountsManager.sharedInstance().init(this);
        AddressesManager.sharedInstance().init(this);
        NodesManager.sharedInstance().init(this);
        mNodeContext = NodesManager.sharedInstance().getCurrentNode();
        nodeInfoBarInit();
        mMainFunctionPage = new MainFunctionPage(this);
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        NodesManager.sharedInstance().release(this);
        AccountsManager.sharedInstance().release(this);
        AddressesManager.sharedInstance().release(this);
        mMainFunctionPage.release();
    }

    @Override
    public void onResume(){
        super.onResume();
        nodeInfoBarUpdate();
        mMainFunctionPage.update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return false;
    }
}
