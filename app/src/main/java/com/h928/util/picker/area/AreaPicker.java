package com.h928.util.picker.area;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.h928.view.R;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.h928.util.picker.area.model.CityModel;
import com.h928.util.picker.area.model.DistrictModel;
import com.h928.util.picker.area.model.ProvinceModel;
import com.h928.util.picker.area.service.XmlParserHandler;
import com.h928.util.picker.area.widget.OnWheelChangedListener;
import com.h928.util.picker.area.widget.WheelView;
import com.h928.util.picker.area.widget.adapters.ArrayWheelAdapter;

public class AreaPicker extends Dialog implements
		View.OnClickListener, OnWheelChangedListener {
	/**
	 * 所有省
	 */
	protected String[] mProvinceDatas;
	/**
	 * key - 省 value - 市
	 */
	protected Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
	/**
	 * key - 市 values - 区
	 */
	protected Map<String, String[]> mDistrictDatasMap = new HashMap<String, String[]>();

	/**
	 * key - 区 values - 邮编
	 */
	protected Map<String, String> mCodeDatasMap = new HashMap<String, String>();


	protected String mCurrentProvinceName; //当前省的名称
	protected String mCurrentCityName; //当前市的名称
	protected String mCurrentDistrictName = ""; //当前区的名称

    protected int  mCurrentProvinceIndex=0; //当前省的索引
    protected int  mCurrentCityIndex=0; //当前市的索引
    protected int  mCurrentDistrictIndex=0; //当前区的索引

	protected String mCurrentCode = "";
	private Context context;
	private WheelView mViewProvince;
	private WheelView mViewCity;
	private WheelView mViewDistrict;
	private Button mBtnConfirm;
    private IOnConfirmListener confirmListener=null;

	public AreaPicker(Context context) {
		super(context, R.style.AreaPicker);
		this.context = context;
		initView(context,null);
	}

    public AreaPicker(Context context,String defaultCode) {
        super(context, R.style.AreaPicker);
        this.context = context;
        initView(context,defaultCode);
    }

	public void initView(Context context,String defaultCode) {
		View view = LayoutInflater.from(context).inflate(R.layout.util_area_picker, null);
		mViewProvince = (WheelView) view.findViewById(R.id.id_province);
		mViewCity = (WheelView) view.findViewById(R.id.id_city);
		mViewDistrict = (WheelView) view.findViewById(R.id.id_district);
		mBtnConfirm = (Button) view.findViewById(R.id.btn_confirm);
		setUpListener();
		setUpData(context,defaultCode);
		setContentView(view);
	}

	public void setOnConfirmListener(IOnConfirmListener listener){
        confirmListener=listener;
    }

	private void setUpListener() {
		// 添加change事件
		mViewProvince.addChangingListener(this);
		// 添加change事件
		mViewCity.addChangingListener(this);
		// 添加change事减
		mViewDistrict.addChangingListener(this);
		// 添加onclick事件
		mBtnConfirm.setOnClickListener(this);
	}

	private void setUpData(Context context,String defaultCode) {
		initProvinceDatas(context,defaultCode);
		mViewProvince.setViewAdapter(new ArrayWheelAdapter<String>(context,mProvinceDatas));
		// 设置可见条目数量
		mViewProvince.setVisibleItems(7);
		mViewCity.setVisibleItems(7);
		mViewDistrict.setVisibleItems(7);
        mViewProvince.setCurrentItem(mCurrentProvinceIndex);
		updateCities(context);
        mViewCity.setCurrentItem(mCurrentCityIndex);
		updateAreas(context);
        mViewDistrict.setCurrentItem(mCurrentDistrictIndex);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
            case R.id.btn_confirm:
                boolean result=confirmListener.onConfirm(mCurrentProvinceName,mCurrentCityName,mCurrentDistrictName, mCurrentCode);
                if(result){
                    this.hide();
                }
                break;
            default:
                break;
		}

	}

	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		// TODO Auto-generated method stub
		if (wheel == mViewProvince) {
			updateCities(context);
		} else if (wheel == mViewCity) {
			updateAreas(context);
		} else if (wheel == mViewDistrict) {
			mCurrentDistrictName = mDistrictDatasMap.get(mCurrentCityName)[newValue];
			mCurrentCode = mCodeDatasMap.get(mCurrentDistrictName);
		}
	}

    /**
     * 根据当前的省，更新市WheelView的信息
     */
    private void updateCities(Context context) {
        int pCurrent = mViewProvince.getCurrentItem();
        mCurrentProvinceName = mProvinceDatas[pCurrent];
        String[] cities = mCitisDatasMap.get(mCurrentProvinceName);
        if (cities == null) {
            cities = new String[] { "" };
        }
        mViewCity.setViewAdapter(new ArrayWheelAdapter<String>(context, cities));
        mViewCity.setCurrentItem(0);
        updateAreas(context);
    }

	/**
	 * 根据当前的市，更新区WheelView的信息
	 */
	private void updateAreas(Context context) {
		int pCurrent = mViewCity.getCurrentItem();
		mCurrentCityName = mCitisDatasMap.get(mCurrentProvinceName)[pCurrent];
		String[] areas = mDistrictDatasMap.get(mCurrentCityName);

		if (areas == null) {
			areas = new String[] { "" };
		}else{
            mCurrentDistrictName=areas[0];
            mCurrentCode = mCodeDatasMap.get(mCurrentDistrictName);
        }
		mViewDistrict.setViewAdapter(new ArrayWheelAdapter<String>(context,areas));
		mViewDistrict.setCurrentItem(0);
	}

	/**
	 * 解析省市区的XML数据
	 */

	protected void initProvinceDatas(Context context,String defaultCode) {
		List<ProvinceModel> provinceList = null;
        String currentProvinceCode=null;
        String currentCityCode=null;
		AssetManager asset = context.getAssets();
		try {
			InputStream input = asset.open("areas_data.xml");
			// 创建一个解析xml的工厂对象
			SAXParserFactory spf = SAXParserFactory.newInstance();
			// 解析xml
			SAXParser parser = spf.newSAXParser();
			XmlParserHandler handler = new XmlParserHandler();
			parser.parse(input, handler);
			input.close();
			// 获取解析出来的数据
			provinceList = handler.getDataList();
			mProvinceDatas = new String[provinceList.size()];

            if(defaultCode!=null) {
                currentProvinceCode = defaultCode.substring(0, 2);
                currentCityCode = defaultCode.substring(0, 4);
            }

            // */ 初始化默认选中的省、市、区
            if (provinceList != null && !provinceList.isEmpty()) {
                mCurrentProvinceName = provinceList.get(0).getName();
                List<CityModel> cityList = provinceList.get(0).getCityList();
                if (cityList != null && !cityList.isEmpty()) {
                    mCurrentCityName = cityList.get(0).getName();
                    List<DistrictModel> districtList = cityList.get(0).getDistrictList();
                    mCurrentDistrictName = districtList.get(0).getName();
                    mCurrentCode = districtList.get(0).getCode();
                }
            }
            // */

			for (int i = 0; i < provinceList.size(); i++) {
				// 遍历所有省的数据
				mProvinceDatas[i] = provinceList.get(i).getName();

                //获取当前省索引
                if(currentProvinceCode!=null && provinceList.get(i).getCode().equals(currentProvinceCode)){
                    mCurrentProvinceIndex=i;
                }
				List<CityModel> cityList = provinceList.get(i).getCityList();
				String[] cityNames = new String[cityList.size()];
				for (int j = 0; j < cityList.size(); j++) {
					// 遍历省下面的所有市的数据
					cityNames[j] = cityList.get(j).getName();

                    //获取当前城市索引
                    if(currentCityCode!=null && cityList.get(j).getCode().equals(currentCityCode)){
                        mCurrentCityIndex=j;
                    }

					List<DistrictModel> districtList = cityList.get(j).getDistrictList();
					String[] distrinctNameArray = new String[districtList.size()];
					DistrictModel[] districtArray = new DistrictModel[districtList.size()];
					for (int k = 0; k < districtList.size(); k++) {
						// 遍历市下面所有区/县的数据
						DistrictModel districtModel = new DistrictModel(districtList.get(k).getName(),
                                districtList.get(k).getCode());

                        // 区/县对于的邮编，保存到mCodeDatasMap
						mCodeDatasMap.put(districtList.get(k).getName(),districtList.get(k).getCode());

                        //获取当前地区索引
                        if(defaultCode!=null && districtList.get(k).getCode().equals(defaultCode)){
                            mCurrentDistrictIndex=k;
                        }

						districtArray[k] = districtModel;
						distrinctNameArray[k] = districtModel.getName();
					}
					// 市-区/县的数据，保存到mDistrictDatasMap
					mDistrictDatasMap.put(cityNames[j], distrinctNameArray);
				}
				// 省-市的数据，保存到mCitisDatasMap
				mCitisDatasMap.put(provinceList.get(i).getName(), cityNames);
			}

		} catch (Throwable e) {
			e.printStackTrace();
		} finally {

		}
	}

	//点击确定监听接口
    public interface IOnConfirmListener{
        public boolean onConfirm(String province,String city,String district,String code);
    }
}
