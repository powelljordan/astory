package jordan.astory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Jordan on 12/29/2015.
 */
public class AddGeofenceFragment extends DialogFragment {
    // region Properties

    private ViewHolder viewHolder;
    AddGeofenceFragmentListener activity;
    EditText nameEditText;
    EditText contentEditText;
    EditText radiusEditText;

    private ViewHolder getViewHolder() {
        return viewHolder;
    }

    AddGeofenceFragmentListener listener;
//    public String nameEditText = null;
    public void setListener(AddGeofenceFragmentListener listener) {
        this.listener = listener;
    }

    // endregion

    // region Overrides

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_geofence_dialog, null);
        viewHolder = new ViewHolder();


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.Add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id){

                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddGeofenceFragment.this.getDialog().cancel();

                        if (listener != null) {
                            listener.onDialogNegativeClick(AddGeofenceFragment.this);
                        }
                    }
                });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.onDialogPositiveClick(AddGeofenceFragment.this);
                            Dialog d = (Dialog) dialog;
                            nameEditText = (EditText) d.findViewById(R.id.fragment_add_geofence_name);
                            contentEditText = (EditText) d.findViewById(R.id.fragment_add_geofence_content);
//                            radiusEditText = (EditText) d.findViewById(R.id.fragment_add_geofence_radius);
                            activity = (AddGeofenceFragmentListener) getActivity();
                            String name = nameEditText.getText().toString();
                            String content = contentEditText.getText().toString();
                            if(dataIsValid(name, content)){
                                activity.onFinishedInputDialog(nameEditText.getText().toString(), contentEditText.getText().toString());
                                dialog.dismiss();
                            }
                            else{
                                Toast.makeText(getContext(), "Please enter text into both fields", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                });

            }
        });


        return dialog;
    }

    public boolean dataIsValid(String name, String content){
        if(!name.equals("") && !content.equals("")){
            return true;
        }
        return false;
    }


    public interface AddGeofenceFragmentListener {
        void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog);
        void onDialogNegativeClick(android.support.v4.app.DialogFragment dialog);
        void onFinishedInputDialog(String name, String content);
    }


    public class ViewHolder {


        public void populate(View v) {
            nameEditText = (EditText) v.findViewById(R.id.fragment_add_geofence_name);
            contentEditText = (EditText)v.findViewById(R.id.fragment_add_geofence_content);


        }
    }
}
