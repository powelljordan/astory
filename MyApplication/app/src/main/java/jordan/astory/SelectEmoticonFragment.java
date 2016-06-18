package jordan.astory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by Jordan on 3/17/2016.
 */
public class SelectEmoticonFragment extends DialogFragment {    // region Properties

    SelectEmoticonFragmentListener activity;

    SelectEmoticonFragmentListener listener;
    public void setListener(SelectEmoticonFragmentListener listener) {
        this.listener = listener;
    }

    // endregion

    // region Overrides

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.select_emoticon_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                LinearLayout genericReaction = (LinearLayout) dialog.findViewById(R.id.generic_upvote);
                LinearLayout happyReaction = (LinearLayout) dialog.findViewById(R.id.happy);
                LinearLayout sadReaction = (LinearLayout) dialog.findViewById(R.id.sad);
                LinearLayout madReaction = (LinearLayout) dialog.findViewById(R.id.mad);
                LinearLayout surprisedReaction = (LinearLayout) dialog.findViewById(R.id.surprised);
                activity = (SelectEmoticonFragmentListener) getActivity();
                genericReaction.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        activity.onFinishedEmoticonSelection("generic");
                        dialog.dismiss();
                    }
                });

                happyReaction.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        activity.onFinishedEmoticonSelection("happy");
                        dialog.dismiss();
                    }
                });

                sadReaction.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        activity.onFinishedEmoticonSelection("sad");
                        dialog.dismiss();
                    }
                });

                madReaction.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        activity.onFinishedEmoticonSelection("mad");
                        dialog.dismiss();
                    }
                });

                surprisedReaction.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        activity.onFinishedEmoticonSelection("surprised");
                        dialog.dismiss();
                    }
                });


            }
        });


        return dialog;
    }



    public interface SelectEmoticonFragmentListener {
        void onFinishedEmoticonSelection(String name);
    }

}
