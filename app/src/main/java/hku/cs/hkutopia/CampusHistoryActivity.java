package hku.cs.hkutopia;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hku.cs.hkutopia.adapter.AlumniAdapter;
import hku.cs.hkutopia.adapter.HistoricalBuildingAdapter;
import hku.cs.hkutopia.adapter.TimelineAdapter;
import hku.cs.hkutopia.model.Alumni;
import hku.cs.hkutopia.model.HistoricalBuilding;
import hku.cs.hkutopia.model.TimelineEvent;

public class CampusHistoryActivity extends AppCompatActivity {

    private RecyclerView timelineRecyclerView;
    private RecyclerView buildingsRecyclerView;
    private RecyclerView alumniRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_history);

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // 初始化RecyclerViews
        initTimelineRecyclerView();
        initBuildingsRecyclerView();
        initAlumniRecyclerView();
    }

    private void initTimelineRecyclerView() {
        timelineRecyclerView = findViewById(R.id.timelineRecyclerView);
        timelineRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 创建时间线数据
        List<TimelineEvent> timelineEvents = new ArrayList<>();
        timelineEvents.add(new TimelineEvent(
                "1911年",
                "香港大学创校",
                "香港政府通过《香港大学条例》正式创办香港大学，开启香港现代高等教育。"));

        timelineEvents.add(new TimelineEvent(
                "1912年",
                "本部大楼启用",
                "本部大楼竣工并迎来首批学生，成为港大百年地标。"));

        timelineEvents.add(new TimelineEvent(
                "1941年至1945年",
                "二战停课与损毁",
                "香港沦陷期间，校园被占用且教学全面停止，直至1946年复课。"));

        timelineEvents.add(new TimelineEvent(
                "1939年",
                "成立理学院",
                "理学院正式成立，标志港大理科教育的发展新篇章。"));

        timelineEvents.add(new TimelineEvent(
                "1967年",
                "成立社会科学院",
                "社会科学院成立，推动政治、心理、社会等跨学科研究。"));

        timelineEvents.add(new TimelineEvent(
                "1982年",
                "成立牙医学院",
                "牙医学院在太古英皇佐治牙科医院成立，成为香港唯一牙科专业教育机构。"));

        timelineEvents.add(new TimelineEvent(
                "2011年",
                "百周年校庆",
                "港大启动百周年庆典，一系列学术及社会活动贯穿2011‑2012年。"));

        timelineEvents.add(new TimelineEvent(
                "2012年",
                "百年校园计划完成",
                "百周年校园（Centennial Campus）建成并启用，校园面积大幅扩展。"));


        // 设置适配器
        TimelineAdapter adapter = new TimelineAdapter(timelineEvents);
        timelineRecyclerView.setAdapter(adapter);

        // 添加动画
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
        timelineRecyclerView.setLayoutAnimation(animation);
    }

    private void initBuildingsRecyclerView() {
        buildingsRecyclerView = findViewById(R.id.buildingsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        buildingsRecyclerView.setLayoutManager(layoutManager);

        // 创建历史建筑数据
        List<HistoricalBuilding> buildings = new ArrayList<>();
        buildings.add(new HistoricalBuilding("本部大楼", "建于1912年", R.drawable.img_main_building));
        buildings.add(new HistoricalBuilding("冯平山图书馆", "建于1932年", R.drawable.hku_fung_ping_shan_library));
        buildings.add(new HistoricalBuilding("陆佑堂", "建于1914年", R.drawable.hku_loke_yew_hall));
        buildings.add(new HistoricalBuilding("明原堂", "建于1919年", R.drawable.hku_may_hall));
        buildings.add(new HistoricalBuilding("爵禄堂", "建于1931年", R.drawable.hku_eliot_hall));

        // 设置适配器
        HistoricalBuildingAdapter adapter = new HistoricalBuildingAdapter(buildings);
        buildingsRecyclerView.setAdapter(adapter);

        // 添加动画
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_slide_right);
        buildingsRecyclerView.setLayoutAnimation(animation);
    }

    private void initAlumniRecyclerView() {
        alumniRecyclerView = findViewById(R.id.alumniRecyclerView);
        alumniRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 创建杰出校友数据
        List<Alumni> alumniList = new ArrayList<>();
        alumniList.add(new Alumni("孙中山", "1892年毕业于香港华人西医书院（港大医学院前身）", "中国近代民主革命的先驱，领导辛亥革命建立中华民国，被尊称为“国父”；他誉港大为“知识的诞生地”，并于1923年回母校演讲，深刻影响后世。", R.drawable.alumni_sun));
        alumniList.add(new Alumni("杨振宁", "1942年毕业于物理系", "1957年诺贝尔物理学奖获得者，因发现宇称不守恒原理而获奖。", R.drawable.alumni_yang));
        alumniList.add(new Alumni("李嘉诚", "荣誉博士", "著名企业家和慈善家，长江实业集团创始人，香港大学的重要捐赠者。", R.drawable.alumni_li));
        alumniList.add(new Alumni("饶宗颐", "1952年任教于中文系", "国学大师，在考古学、历史学、文学、艺术等多个领域有杰出成就。", R.drawable.alumni_rao));
        alumniList.add(new Alumni("徐立之", "1972年毕业于医学院", "著名遗传学家，曾任香港大学校长，发现了多种遗传疾病的基因。", R.drawable.alumni_tsui));

        // 设置适配器
        AlumniAdapter adapter = new AlumniAdapter(alumniList);
        alumniRecyclerView.setAdapter(adapter);

        // 添加动画
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
        alumniRecyclerView.setLayoutAnimation(animation);
    }
}