package com.alphadraco.audioanalyzer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.provider.ContactsContract;
import android.support.annotation.DimenRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Created by aladin on 19.09.2015.
 */
public class SpectralView  extends View {
    Paint paint_grid = new Paint();
    Paint paint_gridy = new Paint();
    Paint paint_subgrid = new Paint();
    Paint paint_fft = new Paint();
    Paint paint_avg = new Paint();
    Paint paint_max = new Paint();
    Paint paint_frame = new Paint();
    Paint paint_note = new Paint();
    Paint paint_colorBarFrame = new Paint();
    Paint paint_mark=new Paint();


    Paint textY = new Paint();
    Paint textX = new Paint();
    Paint textYL = new Paint();

    float [] line1;
    float [] line2;
    float [] line3;


    DataConsolidator dataConsolidator;
    AudioAnalyzerHelper helper;
    AudioAnalyzer audioAnalyzer;

    float[] logf;

    float fmin;
    float fmax;
    float lmin;
    float lmax;

    float trackf;
    Path trackfPath=new Path();

    int pointers;
    int trackmode;
    float PT0X,PT0Y;
    float PT1X,PT1Y;
    float storeLmin,storeLmax;
    float storeFmin,storeFmax;

    float mPreviousX;
    float mPreviousY;

    float mPreviousX2;
    float mPreviousY2;

    boolean gridredraw;

    int xofs,yofs;
    float fctr;
    Rect unitrect;
    Rect maxrect;
    int gridfontheight;
    int fontspace;
    int levelBarHeight;
    int buttonFontHeight;
    int buttonHeight;



    boolean displaywaterfall;
    String colorTabString;
    ColorTable colorTable;

    // Configuration
    boolean islog;
    boolean showAvg;
    boolean showPeak;
    boolean showFFT;
    float ofs=0.0f;
    String unit;
    String note;
    SharedPreferences SpectralPrefs;
    SharedPreferences.OnSharedPreferenceChangeListener PrefListener;

    public void setPreferences(SharedPreferences prefs) {
        SpectralPrefs=prefs;
        if (SpectralPrefs != null) {
            lmin = SpectralPrefs.getFloat("LMIN",-120.0f);
            lmax = SpectralPrefs.getFloat("LMAX", 0.0f);
            fmin = SpectralPrefs.getFloat("FMIN", 100.0f);
            fmax = SpectralPrefs.getFloat("FMAX", 20000.0f);
            islog = SpectralPrefs.getBoolean("SpecDisplayLog", true);
            showAvg = SpectralPrefs.getBoolean("SpecDisplayAvg", true);
            showPeak = SpectralPrefs.getBoolean("SpecDisplayPeak", true);
            showFFT = SpectralPrefs.getBoolean("SpecDisplayFFT", true);
            trackf = SpectralPrefs.getFloat("TrackF", -1.0f);
            colorTabString = SpectralPrefs.getString("WaterFallColor", "KrYW");
            displaywaterfall=SpectralPrefs.getBoolean("DisplayWaterfall",false);
            PrefListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("SpecDisplayLog")) {
                        boolean isl=prefs.getBoolean(key, true);
                        changeMode(isl);
                    }
                    if (key.equals("SpecDisplayAvg"))
                        showAvg = prefs.getBoolean(key, true);
                    if (key.equals("SpecDisplayPeak"))
                        showPeak = prefs.getBoolean(key, true);
                    if (key.equals("WaterFallColor"))
                        colorTabString=prefs.getString(key,"KrYW");
                }
            };
            SpectralPrefs.registerOnSharedPreferenceChangeListener(PrefListener);
        } else
            PrefListener=null;
    }

    public void setup(Context context) {
        float stdsize = new Button(context).getTextSize();
        PrefListener=null;
        SpectralPrefs=null;

        paint_mark.setColor(Color.BLUE);
        paint_mark.setStyle(Paint.Style.STROKE);
        paint_mark.setPathEffect(new DashPathEffect(new float[]{20,20}, 0));
        paint_mark.setStrokeWidth(2);

        paint_frame.setColor(Color.WHITE);
        paint_frame.setStyle(Paint.Style.STROKE);
        paint_frame.setTextAlign(Paint.Align.LEFT);

        paint_colorBarFrame.setColor(Color.GRAY);
        paint_colorBarFrame.setStyle(Paint.Style.STROKE);
        paint_colorBarFrame.setTextAlign(Paint.Align.LEFT);

        paint_note.setColor(Color.WHITE);
        paint_note.setStyle(Paint.Style.STROKE);
        paint_note.setTextAlign(Paint.Align.CENTER);
        paint_note.setTextSize(stdsize);

        paint_grid.setColor(Color.GRAY);
        //paint_grid.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        paint_grid.setTextAlign(Paint.Align.CENTER);
        paint_grid.setTextSize(stdsize * 0.75f);

        paint_gridy.setColor(Color.GRAY);
        // paint_gridy.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        paint_gridy.setTextAlign(Paint.Align.LEFT);
        paint_gridy.setTextSize(stdsize * 0.75f);

        paint_subgrid.setColor(Color.DKGRAY);
        // paint_subgrid.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));


        paint_fft.setColor(Color.GREEN);paint_fft.setStyle(Paint.Style.STROKE);paint_fft.setPathEffect(null);
        paint_fft.setTextAlign(Paint.Align.CENTER);
        paint_avg.setColor(Color.GRAY);paint_avg.setStyle(Paint.Style.STROKE);paint_avg.setPathEffect(null);
        paint_avg.setTextAlign(Paint.Align.CENTER);
        paint_max.setColor(Color.RED);paint_max.setStyle(Paint.Style.STROKE);paint_max.setPathEffect(null);
        paint_max.setTextAlign(Paint.Align.CENTER);
        // paint_max.setStrokeWidth(3);

        textY.setColor(Color.GRAY);
        textY.setTextAlign(Paint.Align.RIGHT);
        textY.setTextSize(stdsize*0.75f);
        String Sw="-100xx";

        textYL.setColor(Color.GRAY);
        textYL.setTextAlign(Paint.Align.LEFT);
        textYL.setTextSize(stdsize*0.75f);

        textX.setColor(Color.GRAY);
        textX.setTextAlign(Paint.Align.CENTER);
        textX.setTextSize(stdsize*0.75f);
        // String Sh="20k";

        Rect rct=new Rect();
        textX.getTextBounds(Sw,0,Sw.length(),rct);
        fctr=rct.height();
        gridfontheight=rct.height();
        fontspace=gridfontheight/5;
        xofs=rct.width()+fontspace;
        yofs =gridfontheight+fontspace;
        levelBarHeight=3*gridfontheight;

        paint_frame.setTextSize(stdsize);
        paint_max.setTextSize(stdsize);
        paint_avg.setTextSize(stdsize);
        paint_fft.setTextSize(stdsize);

        paint_frame.getTextBounds("dBSPL", 0, 5, rct);
        unitrect=new Rect(rct);


        unitrect.offset((int) xofs, 5 + rct.height());
        paint_max.getTextBounds("MAX", 0, 3, rct);
        buttonHeight=rct.height()*2;
        buttonFontHeight=rct.height();
        maxrect=new Rect(rct);
        maxrect.offset(0, 5 + rct.height());

        colorTabString="KrYW";
        colorTable=new ColorTable(256,colorTabString);

        fmin=100;
        fmax=22050;
        lmin=-100;
        lmax=0;
        islog=true;
        showAvg=true;
        showPeak=true;
        showFFT=true;

        ofs=0.0f;
        unit="dBFS";

        trackf=-1;

        gridredraw=true;

        pointers=0;
        PT0X=PT0Y=PT1X=PT1Y=0;
        storeLmin=storeLmax=storeFmin=storeFmax=0;
        note="";

        line1=line2=line3=null;

        displaywaterfall=false;

    }

    public SpectralView(Context context) {
        super(context);
        setup(context);
    }

    public SpectralView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                storeLmin=lmin;
                storeLmax=lmax;
                storeFmin=fmin;
                storeFmax=fmax;
                float trackfpos=0;
                if (islog) {
                    trackfpos= (float) (xofs + (float) (Math.log(trackf/fmin) / Math.log(fmax / fmin) * (getWidth() - 1 - xofs)));
                } else {
                    trackfpos= (float) xofs + (float) ((trackf - fmin) / (fmax - fmin) * (getWidth() - 1 - xofs));
                }

                // First down
                PT0X = e.getX(0);
                PT0Y = e.getY(0);
                if (!displaywaterfall) {
                    if (PT0X > getWidth()-maxrect.width()) {
                        if (PT0Y < 5 + buttonHeight) {
                            showPeak = !showPeak;
                            if (showPeak)
                                dataConsolidator.reset();
                            SharedPreferences.Editor E=SpectralPrefs.edit();
                            E.putBoolean("SpecDisplayPeak",showPeak);
                            E.apply();
                            return true;
                        } else if (PT0Y < 5 + buttonHeight * 2) {
                            showAvg = !showAvg;
                            SharedPreferences.Editor E=SpectralPrefs.edit();
                            E.putBoolean("SpecDisplayAvg",showAvg);
                            E.apply();
                            return true;
                        } else if (PT0Y < 5 + buttonHeight * 3) {
                            showFFT=!showFFT;
                            SharedPreferences.Editor E=SpectralPrefs.edit();
                            E.putBoolean("SpecDisplayFFT",showFFT);
                            E.apply();
                            return true;
                        }
                    }
                }

                if (PT0Y > getHeight()-2*yofs) {
                    islog=!islog;
                    if (islog && (fmin < 1)) {
                        fmin = 1;
                        if (fmax < fmin+100)
                            fmax=fmin+100;
                    }
                    // Store setup
                    SharedPreferences.Editor E=SpectralPrefs.edit();
                    E.putBoolean("SpecDisplayLog",islog);
                    E.putFloat("LMIN", lmin);
                    E.putFloat("LMAX",lmax);
                    E.putFloat("FMIN",fmin);
                    E.putFloat("FMAX",fmax);
                    E.apply();
                    return true;
                }

                pointers++;

                if (!displaywaterfall && (e.getX() > trackfpos-buttonHeight/2) && (e.getX() < trackfpos+buttonHeight/2)) {
                    if (e.getPointerCount() == 1) {
                        trackmode = 1;
                        return true;
                    }
                }

                trackmode=0;
                if (e.getPointerCount()==2) {
                    PT1X=e.getX(1);
                    PT1Y=e.getY(1);
                    pointers++;
                }
                if (e.getPointerCount() > 2)
                    pointers=0;
                break;

            case MotionEvent.ACTION_UP:
                if (pointers==1) {
                    if (trackmode == 1) {
                        /*if (islog) {
                            trackf = (float) Math.exp(Math.log(fmin) + (e.getX() - xofs) * (Math.log(fmax) - Math.log(fmin)) / (getWidth() - 1 - xofs - 1));
                        } else {
                            trackf = fmin + (e.getX() - xofs) * (fmax - fmin) / (getWidth() - 1 - xofs - 1);
                        }*/
                        SharedPreferences.Editor E = SpectralPrefs.edit();
                        E.putFloat("TrackF", trackf);
                        E.apply();
                    }
                    pointers=0;
                    return true;
                }
                if ((pointers > 0) && (SpectralPrefs != null)) {
                    // Store Data
                    SharedPreferences.Editor E=SpectralPrefs.edit();
                    E.putFloat("LMIN",lmin);
                    E.putFloat("LMAX",lmax);
                    E.putFloat("FMIN",fmin);
                    E.putFloat("FMAX",fmax);
                    E.apply();
                }
            case MotionEvent.ACTION_CANCEL:
                pointers=0;
                break;

            case MotionEvent.ACTION_MOVE:
                if (e.getPointerCount() > 2)
                    pointers=0;
                if ((e.getPointerCount()==2) && (pointers == 1)) {
                    if (trackmode == 0) {
                        PT1X = e.getX(1);
                        PT1Y = e.getY(1);
                        pointers = 2;
                    } else {
                        pointers=0; // break it
                    }
                }
                if (pointers==1) {
                    if (trackmode == 0) {
                        float dx = e.getX() - PT0X;
                        float dy = e.getY() - PT0Y;
                        if (islog) {
                            float df = (float) (-dx * (Math.log(storeFmax) - Math.log(storeFmin)) / (getWidth() - 1 - xofs - 1));
                            fmin = (float) Math.exp(Math.log(storeFmin) + df);
                            fmax = (float) Math.exp(Math.log(storeFmax) + df);
                        } else {
                            float df = -dx * (storeFmax - storeFmin) / (getWidth() - 1 - xofs - 1);
                            fmin = storeFmin + df;
                            fmax = storeFmax + df;
                        }
                        if (!displaywaterfall) {
                            float dl = dy * (storeLmax - storeLmin) / (getHeight() - 1 - yofs - 1);
                            lmin = storeLmin + dl;
                            lmax = storeLmax + dl;
                        }
                    } else {
                        float ttf=0;
                        if (islog) {
                            ttf=(float)Math.exp((e.getX()-xofs)/(getWidth() - 1 - xofs)*Math.log(fmax / fmin))*fmin;
                        } else {
                            ttf=(e.getX()-xofs)/(getWidth() - 1 - xofs)*(fmax - fmin)+fmin;
                        }
                        if (ttf < dataConsolidator.f[1])
                            ttf=dataConsolidator.f[1];
                        if (ttf > dataConsolidator.f[dataConsolidator.len/2-1])
                            ttf=dataConsolidator.f[dataConsolidator.len/2-1];
                        trackf=ttf;
                    }
                } else if (pointers==2) {
                    if (e.getPointerCount() < 2) {
                        pointers=0;
                    } else {
                        // Two pointers
                        float x0 = e.getX(0);
                        float y0 = e.getY(0);
                        float x1 = e.getX(1);
                        float y1 = e.getY(1);
                        float cx0 = (PT0X + PT1X) / 2;
                        float cy0 = (PT0Y + PT1Y) / 2;
                        float cx1 = (e.getX(0) + e.getX(1)) / 2;
                        float cy1 = (e.getY(0) + e.getY(1)) / 2;
                        if (Math.abs(x1 - x0) > Math.abs(y1 - y0)) {
                            // horizontal scale and shift
                            float xscale = (PT1X - PT0X)/(x1 - x0);
                            float dx = -(cx1 - cx0)/(getWidth()-1-xofs-1);
                            if (islog) {
                                fmin = (float)Math.exp((Math.log(storeFmax) + Math.log(storeFmin)) / 2 +
                                        dx * (Math.log(storeFmax) - Math.log(storeFmin))
                                        - xscale * (Math.log(storeFmax) - Math.log(storeFmin)) / 2);
                                fmax = (float)Math.exp((Math.log(storeFmax) + Math.log(storeFmin)) / 2 +
                                        dx * (Math.log(storeFmax) - Math.log(storeFmin))
                                        + xscale * (Math.log(storeFmax) - Math.log(storeFmin)) / 2);
                            } else {
                                fmin = (storeFmax + storeFmin) / 2 + dx * (storeFmax - storeFmin) - xscale * (storeFmax - storeFmin) / 2;
                                fmax = (storeFmax + storeFmin) / 2 + dx * (storeFmax - storeFmin) + xscale * (storeFmax - storeFmin) / 2;
                            }
                        } else {
                            if (!displaywaterfall) {
                                // vertical scale and shift
                                float yscale = (PT1Y - PT0Y) / (y1 - y0);
                                float dy = (cy1 - cy0) / (getHeight() - 1 - yofs - 1);
                                lmin = (storeLmax + storeLmin) / 2 + dy * (storeLmax - storeLmin) - yscale * (storeLmax - storeLmin) / 2;
                                lmax = (storeLmax + storeLmin) / 2 + dy * (storeLmax - storeLmin) + yscale * (storeLmax - storeLmin) / 2;
                            }
                        }
                    }
                }
                break;
        }
        return true;
    }

    public String getFstring(float f) {
        float af=Math.abs(f);
        if (f==0) return "0Hz";
        if (af < 0.01) return String.format("%2.0fmHz",f*1000.0f);
        if (af < 0.1) return String.format("%1.2fHz",f);
        if (af < 1) return String.format("%1.1fHz",f);
        if (af < 10) return String.format("%1.0fHz",f);
        if (af < 100) return String.format("%2.0fHz",f);
        if (af < 1000) return String.format("%3.0fHz",f);
        if (af < 10000) return String.format("%1.0fkHz",f/1000.0f);
        if (af < 100000) return String.format("%2.0fkHz",f/1000.0f);
        if (af < 1000000) return String.format("%3.0fkHz",f/1000.0f);
        return String.format("%1.0fMHz",f/1000000.0f);
    }

    public String getdBstring(float db) {
        return String.format("%1.0f",db);
    }

    public void changeMode(boolean newmode) {
        if (newmode == islog) return;
        if (newmode == true) {
            // Make sure fmin and fmax are set in the right way
            if (fmin <= 0) fmin=10;
            if (fmax <= fmin+100) fmax=fmin+100;
        }
        logf=null;
        islog=newmode;
    }


    public void drawFreqGrid(Canvas canvas, int hofs, int vofs, int width, int height, float fmin, float fmax, boolean islog) {

        if (islog) {
            // Logarithmic scale
            int i1 = (int) Math.floor(Math.log10(fmin));
            int i2 = (int) Math.ceil(Math.log10(fmax));
            for (int i = i1; i <= i2; i++) {
                float f = (float) Math.pow(10, i);
                float X = hofs + (float) ((Math.log(f / fmin)) / (Math.log(fmax / fmin)) * (width-1));
                float XN = hofs + (float) ((Math.log(10.0 * f / fmin)) / (Math.log(fmax / fmin)) * (width-1));
                if ((X > hofs) && (X < hofs+width - 1))
                    canvas.drawLine(X, vofs, X, vofs + height - 1, paint_grid);
                if ((X > hofs) && (X < hofs+width-hofs))
                    canvas.drawText(getFstring(f), X, vofs+height+gridfontheight+fontspace, textX);

                if ((XN - X) > 50) {
                    // Subgrid find
                    for (int j = 2; j < 10; j++) {
                        float XY = X + (float) ((Math.log10(j) - 1.0) * (XN - X));
                        if ((XY > hofs) && (XY < hofs+width - 1))
                            canvas.drawLine(XY, vofs, XY, vofs+height - 1, paint_subgrid);
                    }
                } else if (XN - X > 20) {
                    // Subgrid rough
                    float XY = X + (float) (Math.log10(2) - 1.0) * (XN - X);
                    if ((XY > hofs) && (XY < hofs+width - 1))
                        canvas.drawLine(XY, vofs, XY, vofs+height - 1, paint_subgrid);
                    XY = X + (float) (Math.log10(5) - 1.0) * (XN - X);
                    if ((XY > hofs) && (XY < hofs+width - 1))
                        canvas.drawLine(XY, vofs, XY, vofs+height - 1, paint_subgrid);
                }
            }
        } else {
            // Linear scale
            float range=fmax-fmin;
            float gridstep=1;
            float i1,i2;
            if (range < 0.1) gridstep=0.001f;
            else if (range < 1) gridstep=0.01f;
            else if (range < 10) gridstep=0.1f;
            else if (range < 100) gridstep=1f;
            else if (range < 1000) gridstep=10f;
            else if (range < 10000) gridstep=100f;
            else if (range < 100000) gridstep=1000f;
            else gridstep=10000f;
            i1=(float)Math.floor(fmin / gridstep)*gridstep;
            i2=(float)Math.ceil(fmax/gridstep)*gridstep;
            float biggrid=gridstep*10f;


            for (float i=i1;i<=i2;i+=gridstep) {
                float X=hofs+(i-fmin)*(width-1)/(fmax-fmin);
                if (Math.floor(i/biggrid+0.5)==i/biggrid) {
                    if ((X > hofs) && (X < hofs + width - 1))
                        canvas.drawLine(X, vofs, X, vofs+height - 1, paint_grid);
                    if ((X >= hofs) && (X < hofs + width-1))
                        canvas.drawText(getFstring(i),X,vofs+height+gridfontheight+fontspace,textX);
                } else {
                    if ((X > hofs) && (X < hofs + width-1))
                        canvas.drawLine(X, vofs, X, vofs+height - 1, paint_subgrid);
                }
            }
        }

    }

    public void drawGridWaterfall(Canvas canvas, int hofs, int vofs, int width, int height) {

        drawFreqGrid(canvas,hofs,vofs,width,height,fmin,fmax,islog);

        float tmax=(height - 1)/2*dataConsolidator.len/dataConsolidator.fs;
        int step=1;
        if (tmax > 50)
            step=5;
        else if (tmax > 20)
            step=2;
        else
            step=1;
        for (int i=step;i<tmax;i+=step) {
            float Y=vofs+height-1-(float)i/tmax*(height-1);
            canvas.drawLine(hofs, Y, hofs + width - 1, Y, paint_grid);
            if ((Y > fctr) && (Y < height - 1 - fctr))
                canvas.drawText(String.format("%d",i), hofs- fontspace, Y + fctr / 2, textY);
        }

    }

    public void drawGridSpectrum(Canvas canvas, int hofs, int vofs, int width, int height) {

        drawFreqGrid(canvas,hofs,vofs,width,height,fmin,fmax,islog);

        float lminX = lmin + ofs;
        float lmaxX = lmax + ofs;

        int i1 = (int) Math.floor(lminX / 10.0);
        int i2 = (int) Math.ceil(lmaxX / 10.0);
        for (int i = i1; i <= i2; i++) {
            int Y = (int) (vofs + height - 1) - (int) ((i * 10.0 - lminX) * (height - 1) / (lmaxX - lminX));
            if ((Y >= vofs) && (Y < vofs + height))
                canvas.drawLine(hofs, Y, hofs+width - 1, Y, paint_grid);
            if ((Y > fctr) && (Y < height - 1 - yofs - fctr))
                canvas.drawText(getdBstring(i * 10.0f), xofs - fontspace, Y + fctr / 2, textY);
        }

    }

    public void drawGrid(Canvas canvas) {
        int width=canvas.getWidth();
        int height=canvas.getHeight();

        int gwidth=width-(int)xofs-2;
        int gheight=height-(int)yofs-2;

        int hofs=(int)xofs+1;
        int vofs=1;

        if (displaywaterfall) {
            gheight=height-(int)yofs-2-levelBarHeight;
            vofs=1+levelBarHeight;
            drawGridWaterfall(canvas,hofs,vofs,gwidth,gheight);
        } else {
            drawGridSpectrum(canvas, hofs, vofs, gwidth, gheight);
        }

        canvas.drawRect(hofs - 1, vofs - 1, hofs + gwidth - 1 + 1, vofs + gheight - 1 + 1, paint_frame);
        gridredraw=false;

        // Unit
        if (displaywaterfall) {
            canvas.drawText("s", unitrect.left, unitrect.bottom, paint_frame);
            if ((colorTable != null) && (colorTable.bar != null)) {
                int bstart=hofs+hofs;
                int bstop=hofs+gwidth-1-hofs-hofs;
                int barwidth=gridfontheight*7/10;
                int bary1=gridfontheight/2-barwidth/2;
                int bary2=gridfontheight/2+barwidth/2;
                Rect sr=new Rect(0,0,colorTable.table.length-1,1);
                Rect dr=new Rect(bstart,bary1,bstop,bary2);
                canvas.drawBitmap(colorTable.bar,sr,dr,null);
                canvas.drawRect(bstart - 1, bary1 - 1, bstop, bary2, paint_colorBarFrame);
                float lmin=-120+ofs;
                float lmax=0+ofs;
                for (float lvl=(float)Math.floor(lmin/10)*10f;lvl < lmax;lvl+=20.0f) {
                    float x=bstart+(lvl-lmin)*(bstop-bstart)/(lmax-lmin);
                    if ((x >= bstart) && (x <= bstop)) {
                        canvas.drawLine(x,bary2,x,bary2+fontspace,paint_gridy);
                        canvas.drawText(getdBstring(lvl),x,bary2+2*fontspace+gridfontheight,textX);
                    }
                }
                canvas.drawText(unit,bstop+fontspace,gridfontheight,textYL);
            }
        } else {
            canvas.drawText(unit, unitrect.left, unitrect.bottom, paint_frame);

            canvas.drawText("MAX", width - 5-(maxrect.width()+15)/2, 5+buttonHeight/2+buttonFontHeight/2,paint_max);
            canvas.drawText("AVG", width - 5-(maxrect.width()+15)/2, 5+buttonHeight/2+buttonFontHeight/2+buttonHeight,paint_avg);
            canvas.drawText("FFT", width - 5-(maxrect.width()+15)/2, 5+buttonHeight/2+buttonFontHeight/2+2*buttonHeight,paint_fft);

            canvas.drawRect(width - maxrect.width() - 20, 5 + 2, width - 5, 5 + buttonHeight - 2, paint_max);
            canvas.drawRect(width - maxrect.width() - 20, 5+2+buttonHeight, width - 5, 5+buttonHeight-2+buttonHeight, paint_avg);
            canvas.drawRect(width - maxrect.width() - 20, 5+2+2*buttonHeight, width - 5, 5+buttonHeight-2+2*buttonHeight, paint_fft);
        }
        //canvas.drawText(note,xofs+(width-xofs)/2.0f,unitrect.bottom,paint_note);

    }

    @Override
    public void onDraw(Canvas canvas) {
        int width=canvas.getWidth();
        int height=canvas.getHeight();
        int trackidx=-1;

        if ((dataConsolidator!=null) && (dataConsolidator.f != null) && (dataConsolidator.len > 0)) {

            if (displaywaterfall) {
                if (helper != null) {
                    int wd = (int)width-(int)xofs-2;
                    int ht = (int)height-(int)yofs-2-levelBarHeight;
                    boolean renewed=false;
                    if ((helper.specMap == null) ||
                            (helper.specMapWidth != wd) ||
                            (helper.specMapHeight != ht)) {
                        colorTable = new ColorTable(256,colorTabString);
                        renewed=true;
                        helper.SpecViewInit(wd, ht, colorTable.table, fmin, fmax, islog);
                    }
                    if (!renewed && (
                            (fmin != helper.specFmin) || (fmax != helper.specFmax) ||
                            (islog != helper.specLogScale) ||
                            !colorTabString.equals(colorTable.id))) {
                        if (!colorTabString.equals(colorTable.id)) {
                            colorTable = new ColorTable(256,colorTabString);
                            helper.SpecViewInit(wd, ht, colorTable.table, fmin, fmax, islog);
                        } else
                            helper.SpecViewInit(wd, ht, null, fmin, fmax, islog);
                    }
                    canvas.drawBitmap(helper.specMap, xofs + 1, 1 + levelBarHeight, null);
                }
                drawGrid(canvas);
            } else {
                drawGrid(canvas);
                canvas.clipRect(xofs+1,1,width-1-1,height-1-yofs-1);

                if ((helper != null) && (helper.specMap != null)) {
                    helper.SpecViewInit(0,0,null,0,0,false);
                }
                if (trackf > 0) {
                    trackidx = (int) Math.floor(trackf / dataConsolidator.fs * dataConsolidator.len + 0.5);
                }
                int q1, q2, q3;
                q1 = q2 = q3 = 0;

                if ((line1 == null) || (line1.length != (dataConsolidator.f.length - 1) * 4)) {
                    line1 = new float[(dataConsolidator.f.length - 1) * 4];
                    line2 = new float[(dataConsolidator.f.length - 1) * 4];
                    line3 = new float[(dataConsolidator.f.length - 1) * 4];
                }
                ;

                if (islog) {
                    // Logarithmic
                    if ((logf == null) || (logf.length != dataConsolidator.len / 2) ||
                            (logf[0] != (float) Math.log(dataConsolidator.f[0] / fmin))) {
                        if ((logf == null) || (logf.length != dataConsolidator.len / 2))
                            logf = new float[dataConsolidator.len / 2];
                        for (int i = 1; i < logf.length; i++)
                            logf[i] = (float) Math.log(dataConsolidator.f[i] / fmin);
                    }
                    float logfmaxmin = (float) Math.log(fmax / fmin);
                    float y = dataConsolidator.y[1];
                    float yavg = dataConsolidator.yavg[1];
                    float ypeak = dataConsolidator.ypeak[1];
                    float X = (float) xofs + (float) (logf[1] / logfmaxmin * (width - 1 - xofs));
                    float Y = (height - 1 - yofs) - (y - lmin) / (lmax - lmin) * (height - 1 - yofs);
                    float Yavg = (height - 1 - yofs) - (yavg - lmin) / (lmax - lmin) * (height - 1 - yofs);
                    float Ypeak = (height - 1 - yofs) - (ypeak - lmin) / (lmax - lmin) * (height - 1 - yofs);
                    if (0 == trackidx) {
                        if (showPeak) canvas.drawCircle(X, Ypeak, 10f, paint_max);
                        if (showAvg) canvas.drawCircle(X, Yavg, 10f, paint_avg);
                        if (showFFT) canvas.drawCircle(X, Y, 10f, paint_fft);
                    }
                    for (int i = 2; i < logf.length; i++) {
                        float y2 = dataConsolidator.y[i];
                        float yavg2 = dataConsolidator.yavg[i];
                        float ypeak2 = dataConsolidator.ypeak[i];
                        float X2 = (float) xofs + (float) (logf[i] / logfmaxmin * (width - 1 - xofs));
                        float Y2 = (height - 1 - yofs) - (y2 - lmin) / (lmax - lmin) * (height - 1 - yofs);
                        float Yavg2 = (height - 1 - yofs) - (yavg2 - lmin) / (lmax - lmin) * (height - 1 - yofs);
                        float Ypeak2 = (height - 1 - yofs) - (ypeak2 - lmin) / (lmax - lmin) * (height - 1 - yofs);
                        line1[q1++] = X;
                        line1[q1++] = Y;
                        line1[q1++] = X2;
                        line1[q1++] = Y2;
                        line2[q2++] = X;
                        line2[q2++] = Ypeak;
                        line2[q2++] = X2;
                        line2[q2++] = Ypeak2;
                        line3[q3++] = X;
                        line3[q3++] = Yavg;
                        line3[q3++] = X2;
                        line3[q3++] = Yavg2;

                        //if (showPeak) canvas.drawLine(X, Ypeak, X2, Ypeak2, paint_max);
                        //if (showAvg) canvas.drawLine(X, Yavg, X2, Yavg2, paint_avg);
                        //canvas.drawLine(X, Y, X2, Y2, paint_fft);
                        if (i == trackidx) {
                            if (showPeak) canvas.drawCircle(X2, Ypeak2, 10f, paint_max);
                            if (showAvg) canvas.drawCircle(X2, Yavg2, 10f, paint_avg);
                            if (showFFT) canvas.drawCircle(X2, Y2, 10f, paint_fft);
                        }
                        X = X2;
                        Y = Y2;
                        Yavg = Yavg2;
                        Ypeak = Ypeak2;
                    }
                    X = (float) xofs + (float) (Math.log(trackf/fmin) / logfmaxmin * (width - 1 - xofs));
                    trackfPath.reset();
                    trackfPath.moveTo(X, gridfontheight);
                    trackfPath.lineTo(X, height - 1 - yofs - gridfontheight);
                    canvas.drawPath(trackfPath, paint_mark);
                    // canvas.drawLine(X,gridfontheight,X,height-1-yofs-gridfontheight,paint_mark);
                } else {
                    // Linear
                    float f = dataConsolidator.f[1];
                    float y = dataConsolidator.y[1];
                    float yavg = dataConsolidator.yavg[1];
                    float ypeak = dataConsolidator.ypeak[1];
                    float X = (float) xofs + (float) ((f - fmin) / (fmax - fmin) * (width - 1 - xofs));
                    float Y = (height - 1 - yofs) - (y - lmin) / (lmax - lmin) * (height - 1 - yofs);
                    float Yavg = (height - 1 - yofs) - (yavg - lmin) / (lmax - lmin) * (height - 1 - yofs);
                    float Ypeak = (height - 1 - yofs) - (ypeak - lmin) / (lmax - lmin) * (height - 1 - yofs);
                    if (0 == trackidx) {
                        if (showPeak) canvas.drawCircle(X, Ypeak, 10f, paint_max);
                        if (showAvg) canvas.drawCircle(X, Yavg, 10f, paint_avg);
                        if (showFFT) canvas.drawCircle(X, Y, 10f, paint_fft);
                    }
                    for (int i = 2; i < dataConsolidator.len / 2; i++) {
                        float f2 = dataConsolidator.f[i];
                        float y2 = dataConsolidator.y[i];
                        float yavg2 = dataConsolidator.yavg[i];
                        float ypeak2 = dataConsolidator.ypeak[i];
                        float X2 = (float) xofs + (float) ((f2 - fmin) / (fmax - fmin) * (width - 1 - xofs));
                        float Y2 = (height - 1 - yofs) - (y2 - lmin) / (lmax - lmin) * (height - 1 - yofs);
                        float Yavg2 = (height - 1 - yofs) - (yavg2 - lmin) / (lmax - lmin) * (height - 1 - yofs);
                        float Ypeak2 = (height - 1 - yofs) - (ypeak2 - lmin) / (lmax - lmin) * (height - 1 - yofs);
                        line1[q1++] = X;
                        line1[q1++] = Y;
                        line1[q1++] = X2;
                        line1[q1++] = Y2;
                        line2[q2++] = X;
                        line2[q2++] = Ypeak;
                        line2[q2++] = X2;
                        line2[q2++] = Ypeak2;
                        line3[q3++] = X;
                        line3[q3++] = Yavg;
                        line3[q3++] = X2;
                        line3[q3++] = Yavg2;
                        //if (showPeak) canvas.drawLine(X, Ypeak, X2, Ypeak2, paint_max);
                        //if (showAvg) canvas.drawLine(X, Yavg, X2, Yavg2, paint_avg);
                        //canvas.drawLine(X, Y, X2, Y2, paint_fft);
                        if (i == trackidx) {
                            if (showPeak) canvas.drawCircle(X2, Ypeak2, 10f, paint_max);
                            if (showAvg) canvas.drawCircle(X2, Yavg2, 10f, paint_avg);
                            if (showFFT) canvas.drawCircle(X2, Y2, 10f, paint_fft);
                        }
                        X = X2;
                        Y = Y2;
                        Yavg = Yavg2;
                        Ypeak = Ypeak2;
                    }
                    X = (float) xofs + (float) ((trackf - fmin) / (fmax - fmin) * (width - 1 - xofs));
                    trackfPath.reset();
                    trackfPath.moveTo(X, gridfontheight);
                    trackfPath.lineTo(X, height - 1 - yofs - gridfontheight);
                    canvas.drawPath(trackfPath, paint_mark);
                    // canvas.drawLine(X,gridfontheight,X,height-1-yofs-gridfontheight,paint_mark);
                }
                if (showPeak) canvas.drawLines(line2, paint_max);
                if (showAvg) canvas.drawLines(line3, paint_avg);
                if (showFFT) canvas.drawLines(line1, paint_fft);
            }
            canvas.clipRect(0,0,width-1,height-1);
        }

        //    canvas.drawLine(0, 0, width-1, height-1, paint);
        //    canvas.drawLine(width-1, 0, 0, height-1, paint);
    }

    public void display(DataConsolidator dc) {
        dataConsolidator=dc;
        invalidate();
    }

}
