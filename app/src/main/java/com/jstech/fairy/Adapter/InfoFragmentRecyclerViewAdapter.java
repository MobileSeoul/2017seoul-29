package com.jstech.fairy.Adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jstech.fairy.DataType.InfoDataType;
import com.jstech.fairy.InfoDetail;
import com.jstech.fairy.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Context.MODE_PRIVATE;


public class InfoFragmentRecyclerViewAdapter extends RecyclerView.Adapter<InfoFragmentRecyclerViewAdapter.ViewHolder>{

    Context mContext;
    ArrayList<InfoDataType> aListInfo;
    SQLiteDatabase mSQLiteDatabase;

    private int mYear,mMonth,mDay;
    private String today,endday,dday;

    public InfoFragmentRecyclerViewAdapter(Context context, ArrayList<InfoDataType> aListInfo) {
        this.mContext = context;
        this.aListInfo = aListInfo;
    }

    @Override
    public InfoFragmentRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_info,null);
        return new ViewHolder(v);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)


    @Override
    public void onBindViewHolder(InfoFragmentRecyclerViewAdapter.ViewHolder holder, int position) {

        final int pos = position;

        //d-day
        Calendar cal = new GregorianCalendar();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH)+1;
        mDay = cal.get(Calendar.DAY_OF_MONTH);
        String tempmonth;
        String tempday;
        if(((mMonth)/10) <1)
            tempmonth = "0"+String.valueOf(mMonth);
        else
            tempmonth =String.valueOf(mMonth);
        if((mDay/10)<1)
            tempday = "0"+String.valueOf(mDay);
        else
            tempday =String.valueOf(mDay);
        endday = aListInfo.get(pos).getStrEndDate();
        String temp[] = endday.split("-");
        endday = temp[0]+temp[1]+temp[2];
        today = String.valueOf(mYear)+tempmonth+tempday;
        try {
            dday = Long.toString(diffOfDate(today, endday));
            if(dday.equals("0"))
                holder.tvDDay.setText("D-Day");
            else {
                holder.tvDDay.setText("D-"+dday);
            }
        }catch (Exception e){}


        //  이미지 스트링이 없으면 비운다.
        if(aListInfo.get(pos).getStrMainImg().isEmpty())
        {
            holder.ivImg.setVisibility(View.GONE);
        }
        else
        {
            //  Picasso 라이브러리를 통해 URL에 해당하는 이미지를 가져와 뷰에 넣는다.
            Picasso.with(mContext).load(aListInfo.get(pos).getStrMainImg())
                    .placeholder(R.drawable.loading_image)                              // 이미지 불러오는 동안 이미지
                    .error(R.drawable.no_image2)                                         // 다운로드 실패 시, 이미지
                    .fit()                                                               // 이미지뷰에 맞추기
                    .into(holder.ivImg);
        }

        //  카드뷰의 각 값 세팅
        holder.tvTitle.setText(aListInfo.get(pos).getStrTitle());
        holder.tvDate.setText(aListInfo.get(pos).getStrStartDate()+"~"+aListInfo.get(pos).getStrEndDate());
        holder.tvPlace.setText(aListInfo.get(pos).getStrPlace());

        //  무/유료 구분
        String strIsFree = aListInfo.get(pos).getStrIsFree();
        if(strIsFree.equals("1"))
        {
            holder.tvFee.setText(mContext.getString(R.string.text_free));
        }
        else if(strIsFree.equals("0"))
        {
            holder.tvFee.setText(mContext.getString(R.string.text_not_free));
        }
        else
        {
            holder.tvFee.setText(strIsFree);
        }

        //  최초 좋아요 ImageView는 DB 값으로부터 구해온 IsHeart 값을 통해 구분.
        //  이후에는 클릭 이벤트 참조.
        if(aListInfo.get(pos).getStrIsHeart() == "1")       //  좋아요 눌러진 경우.
        {
            holder.ivHeart.setImageResource(R.drawable.ic_heart_full);
        }
        else
        {
            holder.ivHeart.setImageResource(R.drawable.ic_heart_empty);
        }

        //  카드뷰 클릭 이벤트
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //  세부 보여주기 액티비티로 옮길 데이터 복사
                InfoDataType infoData = new InfoDataType();
                infoData.CopyData(aListInfo.get(pos));

                Intent intent = new Intent(mContext.getApplicationContext(), InfoDetail.class);
                intent.putExtra("InfoData", infoData);
                mContext.startActivity(intent);

            }
        });

        //  좋아요 버튼 클릭 이벤트
        holder.ivHeart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                //  좋아요 안눌러짐 -> 눌러짐
                if(aListInfo.get(pos).getStrIsHeart() == "0")
                {
                    PushHeart(pos);
                }
                else
                {
                    CancelHeart(pos);
                }

            }
        });

    }

    //  Database의 Heart 테이블에 데이터 삽입. (테이블이 없으면 이곳에서 생성한다.)
    public void PushHeart(int iPos)
    {
        mSQLiteDatabase = mContext.openOrCreateDatabase(mContext.getString(R.string.database_name), MODE_PRIVATE, null);
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + mContext.getString(R.string.heart_table_name)
                + " (CULTCODE VARCHAR(32), SUBJCODE VARCHAR(32), CODENAME VARCHAR(128),"
                + " TITLE VARCHAR(512), START_DATE VARCHAR(16), END_DATE VARCHAR(16),"
                + " TIME VARCHAR(512), PLACE VARCHAR(1024), ORG_LINK VARCHAR(1024),"
                + " MAIN_IMG VARCHAR(1024), USE_TARGET VARCHAR(1024), USE_FEE VARCHAR(1024),"
                + " SPONSOR VARCHAR(1024), INQUIRY VARCHAR(1024), IS_FREE VARCHAR(128),"
                + " TICKET VARCHAR(1024), CONTENTS VARCHAR(2048));");

        mSQLiteDatabase.execSQL("INSERT INTO " + mContext.getString(R.string.heart_table_name)
                + " (CULTCODE, SUBJCODE, CODENAME, TITLE, START_DATE, END_DATE, TIME, PLACE, ORG_LINK,"
                + " MAIN_IMG, USE_TARGET, USE_FEE, SPONSOR, INQUIRY, IS_FREE, TICKET, CONTENTS) Values"
                + " ('"+ aListInfo.get(iPos).getStrCultCode() + "', '"
                + aListInfo.get(iPos).getStrSubjCode() + "', '"
                + aListInfo.get(iPos).getStrCodeName() + "', '"
                + aListInfo.get(iPos).getStrTitle() + "', '"
                + aListInfo.get(iPos).getStrStartDate() + "', '"
                + aListInfo.get(iPos).getStrEndDate() + "', '"
                + aListInfo.get(iPos).getStrTime() + "', '"
                + aListInfo.get(iPos).getStrPlace() + "', '"
                + aListInfo.get(iPos).getStrOrgLink() + "', '"
                + aListInfo.get(iPos).getStrMainImg() + "', '"
                + aListInfo.get(iPos).getStrUseTarget() + "', '"
                + aListInfo.get(iPos).getStrUseFee() + "', '"
                + aListInfo.get(iPos).getStrSponsor() + "', '"
                + aListInfo.get(iPos).getStrInquiry() + "', '"
                + aListInfo.get(iPos).getStrIsFree() + "', '"
                + aListInfo.get(iPos).getStrTicket() + "', '"
                + aListInfo.get(iPos).getStrContents() + "');");

        mSQLiteDatabase.close();

        InfoDataType newInfoData = new InfoDataType();
        newInfoData.CopyData(aListInfo.get(iPos));
        newInfoData.setStrIsHeart("1");             //  좋아요 눌렀으므로 데이터 변경.

        aListInfo.set(iPos, newInfoData);          //  List의 데이터도 변경.
        notifyItemChanged(iPos);                    //   데이터 변경을 알림.
    }

    //  Database의 Heart 테이블에 있는 데이터 삭제
    public void CancelHeart(int iPos)
    {
        mSQLiteDatabase = mContext.openOrCreateDatabase(mContext.getString(R.string.database_name), MODE_PRIVATE, null);
        mSQLiteDatabase.execSQL("DELETE FROM " + mContext.getString(R.string.heart_table_name)
                + " WHERE CULTCODE = " + aListInfo.get(iPos).getStrCultCode());

        mSQLiteDatabase.close();

        InfoDataType newInfoData = new InfoDataType();
        newInfoData.CopyData(aListInfo.get(iPos));
        newInfoData.setStrIsHeart("0");             //  좋아요 취소했으므로 데이터 변경.

        aListInfo.set(iPos, newInfoData);          //  List의 데이터도 변경.
        notifyItemChanged(iPos);                    //   데이터 변경을 알림.
    }

    //  Heart 탭에서 좋아요 누르거나 취소했다는 정보를 알림받아서, Info에서도 적용한다.
    public void HeartDataUpdate(boolean bPushHeart, int iPosition)
    {
        InfoDataType newInfoData = new InfoDataType();
        newInfoData.CopyData(aListInfo.get(iPosition));
        if(bPushHeart == true)
        {
            newInfoData.setStrIsHeart("1");
        }
        else
        {
            newInfoData.setStrIsHeart("0");
        }

        aListInfo.set(iPosition, newInfoData);
        notifyItemChanged(iPosition);
    }

    @Override
    public int getItemCount() {
        return aListInfo.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CardView cardview;
        ImageView ivImg;
        TextView tvTitle;
        TextView tvDate;
        TextView tvPlace;
        TextView tvFee;
        ImageView ivHeart;          //  좋아요 버튼으로 사용할 ImageView.
        TextView tvDDay;

        public ViewHolder(View itemView) {
            super(itemView);
            cardview = (CardView)itemView.findViewById(R.id.info_cardview);
            ivImg = (ImageView)itemView.findViewById(R.id.info_cardview_imageview);
            tvTitle = (TextView)itemView.findViewById(R.id.info_cardview_title);
            tvDate = (TextView)itemView.findViewById(R.id.info_cardview_date);
            tvPlace = (TextView)itemView.findViewById(R.id.info_cardview_place);
            tvFee = (TextView)itemView.findViewById(R.id.info_cardview_fee);
            ivHeart = (ImageView)itemView.findViewById(R.id.info_cardview_btn_heart);
            tvDDay = (TextView)itemView.findViewById(R.id.info_cardview_due);
        }
    }

    public static long diffOfDate(String begin, String end) throws Exception
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        Date beginDate = formatter.parse(begin);
        Date endDate = formatter.parse(end);

        long diff = endDate.getTime() - beginDate.getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffDays;
    }
}
