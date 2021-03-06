package net.rdrei.android.dirchooser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.TextView;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class DirectoryChooserFragmentTest {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void startFragment(@NonNull Fragment fragment, @Nullable Class activityClass) {
        if (activityClass == null) {
            activityClass = Activity.class;
        }

        Activity activity = (Activity) Robolectric.buildActivity(activityClass)
                .create()
                .start()
                .resume()
                .get();

        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main, fragment);
        fragmentTransaction.commit();

        fragmentManager.executePendingTransactions();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void testWithDirectory() {
        final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance("mydir",
                null);

        startFragment(fragment, DirectoryChooserActivityMock.class);

        final View chooseBtn = fragment.getActivity().findViewById(R.id.btnConfirm);
        assertThat(chooseBtn).isEnabled();

        assertTrue(chooseBtn.performClick());
        assertNotNull(((DirectoryChooserActivityMock) fragment.getActivity()).selectedDirectory);
    }

    @Test
    public void testCreateDirectoryDialogAllowFolderNameModification() {
        final String directoryName = "mydir";
        final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance(
                directoryName, null, false, true);

        startFragment(fragment, DirectoryChooserActivityMock.class);

        fragment.onOptionsItemSelected(new TestMenuItem() {
            @Override
            public int getItemId() {
                return R.id.new_folder_item;
            }
        });

        final AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        final ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(dialog);
        assertEquals(shadowAlertDialog.getTitle(), "Create folder");
        assertTrue(shadowAlertDialog.isShowing())

        final Button positiveBtn = shadowAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        assertNotNull(positiveBtn);

        final TextView msgView = (TextView) dialog.findViewById(R.id.msgText);
        assertEquals(msgView.getText().toString(), "Create new folder with name \"mydir\"?");

        final EditText editText = (EditText) dialog.findViewById(R.id.editText);
        assertTrue(editText.getVisibility() == View.VISIBLE)
        assertEquals(editText.getText().toString(), "mydir");
    }

    @Test
    public void testCreateDirectoryDialogDisallowFolderNameModification() {
        final String directoryName = "mydir";
        final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance(
                directoryName, null, false, false);

        startFragment(fragment, DirectoryChooserActivityMock.class);

        fragment.onOptionsItemSelected(new TestMenuItem() {
            @Override
            public int getItemId() {
                return R.id.new_folder_item;
            }
        });

        final AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        final ShadowAlertDialog shadowAlertDialog = Robolectric.shadowOf(dialog);
        assertEquals(shadowAlertDialog.getTitle(), "Create folder");
        assertTrue(shadowAlertDialog.isShowing())

        final Button positiveBtn = shadowAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        assertNotNull(positiveBtn);

        final TextView msgView = (TextView) dialog.findViewById(R.id.msgText);
        assertEquals(msgView.getText().toString(), "Create new folder with name \"mydir\"?");

        final EditText editText = (EditText) dialog.findViewById(R.id.editText);
        assertTrue(editText.getVisibility() == View.GONE)
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void testWithCustomListener() {
        final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance("mydir",
                null);

        startFragment(fragment, CustomDirectoryChooserActivity.class);
        final CustomDirectoryChooserListener listener = new CustomDirectoryChooserListener();
        fragment.setDirectoryChooserListener(listener);

        final View chooseBtn = fragment.getActivity().findViewById(R.id.btnConfirm);
        assertThat(chooseBtn).isEnabled();

        assertTrue(chooseBtn.performClick());
        assertNotNull(listener.selectedDirectory);
    }

    static final private class DirectoryChooserActivityMock extends Activity implements
            DirectoryChooserFragment.OnFragmentInteractionListener {
        public String selectedDirectory;

        @Override
        public void onCreate(@Nullable Bundle bundle) {
            setContentView(R.layout.directory_chooser_activity);
        }

        @Override
        public void onSelectDirectory(@NonNull String path) {
            this.selectedDirectory = path;
        }

        @Override
        public void onCancelChooser() {

        }
    }

    private static class CustomDirectoryChooserActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setContentView(R.layout.directory_chooser_activity);
        }
    }

    private static class CustomDirectoryChooserListener implements
            DirectoryChooserFragment.OnFragmentInteractionListener {
        public String selectedDirectory;

        @Override
        public void onSelectDirectory(@NonNull String path) {
            selectedDirectory = path;
        }

        @Override
        public void onCancelChooser() {
            selectedDirectory = null;
        }
    }
}
