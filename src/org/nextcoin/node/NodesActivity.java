package org.nextcoin.node;

import java.util.LinkedList;

import org.nextcoin.nxtclient.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NodesActivity extends Activity {

    private NodesListView mNodesListView;
    private LinkedList<NodeContext> mNodeList;
    private NodeContext.NodeUpdateListener mNodeUpdateListener = 
            new NodeContext.NodeUpdateListener(){
        @Override
        public void onUpdate(NodeContext node) {
            mNodesListView.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nodes_activity);

        LinkedList<String>  nodeIPList = NodesManager.sharedInstance().getNodeIPList();
        mNodeList = new LinkedList<NodeContext>();
        for ( int i = 0; i < nodeIPList.size(); ++ i ){
            NodeContext node = new NodeContext();
            node.setIP(nodeIPList.get(i));
            node.setNodeUpdateListener(mNodeUpdateListener);
            mNodeList.addLast(node);
        }
        
        mNodesListView = (NodesListView)this.findViewById(R.id.listview_nodes);
        mNodesListView.setNodesList(mNodeList);
        mNodesListView.setOnItemClickListener(new NodesListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                openItemMenu(arg2);
            }});
        
        Button btnAdd = (Button)this.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChangeNodeDialog();
            }
        });

        mItemOptions = new CharSequence[2];
        mItemOptions[0] = this.getText(R.string.select);
        mItemOptions[1] = this.getText(R.string.remove);
    }
    
    @Override
    public void onResume(){
        super.onResume();
        for ( NodeContext node : mNodeList ){
            node.update();
        }
    }
    
    /**
     * items menu --- select, remove
     */
    private CharSequence[] mItemOptions;
    private int mCurrentItemPos;
    private void openItemMenu(int pos){
        if ( null == mNodeList || 0 == mNodeList.size() )
            return;
        
        mCurrentItemPos = pos;
        new AlertDialog.Builder(this)
        .setTitle(mNodeList.get(pos).getIP())
        .setItems(mItemOptions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if ( 0 == which ){
                    NodesManager.sharedInstance().getCurrentNode()
                        .setIP(mNodeList.get(mCurrentItemPos).getIP());
                    setResult(RESULT_OK, null);
                    finish();
                }else if (1 == which ){
                    mNodeList.remove(mCurrentItemPos);
                    NodesManager.sharedInstance().removeNode(mCurrentItemPos);
                    mNodesListView.notifyDataSetChanged();
                }
            }
        })
        .show();
    }    

    /**
     * input node
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
                    if ( ipStr.length() < 5 ){
                        Toast.makeText(NodesActivity.this, R.string.ip_format_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    NodesManager.sharedInstance().addNodeIP(ipStr);
                    NodeContext node = new NodeContext();
                    node.setIP(ipStr);
                    node.setNodeUpdateListener(mNodeUpdateListener);
                    mNodeList.addLast(node);
                    node.update();
                }})
            .setNegativeButton(R.string.back, null)
            .create();
        }
        mNodeChangeDialog.show();        
    }
    
}
