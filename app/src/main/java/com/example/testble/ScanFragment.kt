package com.example.testble

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.testble.databinding.FragmentScanBinding
import com.google.android.material.snackbar.Snackbar
import org.altbeacon.beacon.*


class ScanFragment : Fragment() {

    lateinit var myViewModel: ScanViewModel
    lateinit var binding: FragmentScanBinding

    lateinit var beaconManager: BeaconManager
    lateinit var region: Region

    var flagBluetoothExist:Boolean=false
    val ble_adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val REQUEST_CODE_ACCESS_FINE_LOCATION = 2


    companion object {
        protected const val TAG = "Monitoring"
    }

    val rangeNotifier = object : RangeNotifier {
        override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
            Log.d(TAG, "in didRangeBeacon")
            if (beacons!!.size > 0)
            {
                Log.d(TAG, "didRangeBeaconsInRegion called count:  " + beacons.size + beacons.iterator().next().id1)
                Toast.makeText(requireContext(),"beacon size is ${beacons.size} & id is  ${beacons.iterator().next().id1}",Toast.LENGTH_LONG).show()
            }
            else
            Toast.makeText(requireContext(),"beacon size is ${beacons.size} & nothing found ",Toast.LENGTH_LONG).show()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("lifecycl", "it is oncreate ")
        super.onCreate(savedInstanceState)

        BeaconManager.setDebug(true)

        beaconManager = BeaconManager.getInstanceForApplication(requireContext()).apply {
            foregroundScanPeriod = 7000L
            foregroundBetweenScanPeriod = 5000L
            updateScanPeriods()
            beaconParsers.clear()
            beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-8,i:4-19,i:20-21,i:22-23,p:24-24"))
            region = Region("prefixRegion", Identifier.parse("0x0000000000"), null, null)

        }
        setupPermissions()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        myViewModel = ViewModelProvider(this).get(ScanViewModel::class.java)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scan,container,false)

        binding.apply {
            this.lifecycleOwner = viewLifecycleOwner
            this.scanvm = myViewModel
        }

        // Inflate the layout for this fragment
        return binding.root
    }


    private fun setupPermissions() {
        if (Build.VERSION.SDK_INT > 23) {
            val permission =
                requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            if (permission != PackageManager.PERMISSION_GRANTED) {//if do not have a permission

                //shouldShowReqestPermissionRationale will return true only if the application
                // was launched earlier and the user "denied" the permission WITHOUT checking "never ask again".
                //In other cases (app launched first time, or the app launched earlier too and the user denied permission by
                // checking "never ask again"), the return value is false.

                if (requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    val builder = AlertDialog.Builder(this.requireContext())
                    builder.setMessage("Need GPS Permission")
                        .setTitle("Required Permission")

                    builder.setPositiveButton("OK") { dialog, id -> makeRequest() }

                    val dialog = builder.create()
                    dialog.show()
                } else {
                    makeRequest()
                }

            }

            val permission2 =
                requireActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (permission2 != PackageManager.PERMISSION_GRANTED) {//if do not have a permission

                //shouldShowRequestPermissionRationale will return true only if the application
                // was launched earlier and the user "denied" the permission WITHOUT checking "never ask again".
                //In other cases (app launched first time, or the app launched earlier too and the user denied permission by
                // checking "never ask again"), the return value is false.

                if (requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    val builder = AlertDialog.Builder(this.context)
                    builder.setMessage("برای کارکرد صحیح این برنامه نیاز به دسترسی جی پی اس است.")
                        .setTitle("اجازه دسترسی")

                    builder.setPositiveButton("تایید") { dialog, id -> makeRequest() }

                    val dialog = builder.create()
                    dialog.show()
                } else {
                    makeRequest()
                }

            }
        }
    }


    private fun makeRequest() {
        if (Build.VERSION.SDK_INT > 23) {
            requireActivity().requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE_ACCESS_FINE_LOCATION
            )
        }
    }

    fun rangingButtonTapped() {
        if(checkGPS_BLE()) {
            if (beaconManager.rangedRegions.size == 0) {
                beaconManager.addRangeNotifier(rangeNotifier)
                beaconManager.startRangingBeacons(region)
                binding.scanvm?.isScanning?.value = true
            } else {
                beaconManager.stopRangingBeacons(region)
                binding.BTNScan.run {
                    Handler(Looper.getMainLooper()).postDelayed({ stopAnimation() }, 1000)
                    Handler(Looper.getMainLooper()).postDelayed({ revertAnimation() }, 2000)
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.BTNScan.setOnClickListener {
            rangingButtonTapped()
        }

        binding.scanvm?.isScanning?.observe(viewLifecycleOwner, Observer { currentStatusScan ->
            if (currentStatusScan) {
                object : CountDownTimer(8500, 1000) {
                    override fun onTick(p0: Long) {

                    }

                    override fun onFinish() {
                        beaconManager.stopRangingBeacons(region)
                        beaconManager.removeRangeNotifier(rangeNotifier)
                        binding.scanvm?.isScanning?.value = false
                    }
                }.start()
            }
        })
    }

    private fun checkGPS_BLE():Boolean
    {
        val managerGps = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        flagBluetoothExist = requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        (activity as AppCompatActivity?)!!.apply {supportActionBar?.setTitle(null)}


        if (flagBluetoothExist)
        {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            if (!ble_adapter.isEnabled)
                ble_adapter.enable()

            if (!managerGps.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                val myWarning = bsdGpsWarn()
                myWarning.show(this.parentFragmentManager, "")
                Log.d("off", "gps is off")
                return false
            }
        }
        else
        {
            val detail = "your phone does not support ble"
            Snackbar.make(requireContext(), this.requireView(), detail, Snackbar.LENGTH_INDEFINITE).show()
            return false
        }
        return true
    }


}




