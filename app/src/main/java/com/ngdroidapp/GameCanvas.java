package com.ngdroidapp;

import android.app.AppOpsManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Window;

import java.util.Random;
import java.util.Vector;

import istanbul.gamelab.ngdroid.base.BaseCanvas;
import istanbul.gamelab.ngdroid.core.AppManager;
import istanbul.gamelab.ngdroid.core.NgMediaPlayer;
import istanbul.gamelab.ngdroid.util.Log;
import istanbul.gamelab.ngdroid.util.Utils;


/**
 * Created by noyan on 24.06.2016.
 * Nitra Games Ltd.
 */


public class GameCanvas extends BaseCanvas {

    private Bitmap tileset, spritesheet, bullet, enemy, explode, laser;
    private Rect tilesrc, tiledst, spritesrc, spritedst, bulletsrc, enemysrc, enemydst,explodesrc, explodedst;
    private Rect lasersrc, laserdst1 , laserdst2;
    private int kareno, animasyonno, animasyonyonu, bulletoffsetx_temp, bulletoffsety_temp;
    private Bitmap buttons;
    private Rect playsrc, playdst , restartsrc, restartdst,exitsrc,exitdst;
    private int hiz, hizx, hizy, spritex, spritey,  bulletspeedy, bulletspeed;
    private int bulletx_temp, bullety_temp;//ekrana basılacak merminin koordinatları
    private int explodeframeno; // image'deki src için. ilk 64x64 lük kısım manuel alıyoruz. fonksiyonda tersten gidecez ters cevir.
    private boolean enemyexist , exploded;
    private int sesefekti_patlama; // müzigin bir kere calıp bitmesi icin
    private int enemyspeedx, enemyspeedy,enemyx, enemyy, donmenoktasi;
    private long prevtime, time; // robotun mermi sıklıgını belirleyecek
    private NgMediaPlayer arkaplan_muzik;
    private boolean donmeboolean , enemyfire ;
    public boolean spriteexist;
    private Random enemyrnd;
    private int laserspeed , lasery , laser1x , laser2x; // laser1x ve laser2x , lazerin ateslendikten sonraki güzergahı , konumu icin lazım
    public Vector <Rect> bulletdst; //jenerik yapı ->
    public Vector <Integer>  bulletx2, bullety2, bulletoffsetx2, bulletoffsety2, bulletspeedx2, bulletspeedy2;
    private boolean guishow;
    private boolean playshow;

    int touchx, touchy;//Ekranda bastigimiz yerlerin koordinatlari

    public GameCanvas(NgApp ngApp) {
        super(ngApp);
    }

    public void setup() {
        arkaplan_muzik = new NgMediaPlayer(root);
        arkaplan_muzik.load("sounds/m2.mp3");
        arkaplan_muzik.setVolume(0.5f);
        arkaplan_muzik.prepare();
        arkaplan_muzik.start();

        guishow = false;
        buttons = Utils.loadImage(root,"images/buttons.png");
        playsrc = new Rect();
        playdst = new Rect();
        restartsrc = new Rect();
        restartdst = new Rect();
        exitdst= new Rect();
        exitsrc = new Rect();
        playshow=true;



        try{
            sesefekti_patlama = root.soundManager.load("sounds/se1.wav");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        tileset = Utils.loadImage(root,"images/tilea2.png");
        tilesrc = new Rect();
        tiledst = new Rect();

        spritesheet = Utils.loadImage(root,"images/cowboy.png");
        spriteexist=true;
        spritesrc = new Rect();
        spritedst = new Rect();

        bullet = Utils.loadImage(root,"images/bullet.png");
        bulletsrc = new Rect();
        // REGION KODLARIN ACILIP KAPANMASINI SAGLAR SOL TARAFTAN TOPLU DURSUN
         // region enemy
        enemy = Utils.loadImage(root,"images/mainship03.png");
        enemysrc = new Rect();
        enemydst = new Rect();
        enemyexist = true;
        enemyfire=false;
        enemyspeedx=10;
        enemyspeedy=0;
        enemyx=getWidthHalf()-128; // ekranın ortasından baslasın
        enemyy = getHeight()-256; // alttan baslasın

        donmenoktasi=getWidth();
        donmeboolean=true;
        enemyrnd = new Random(); // rastgele sayı üretme 0-1 arası

        laser = Utils.loadImage(root,"images/beams1.png");
        lasersrc =new Rect();
        laserdst1 = new Rect(-100,-100,-100,-100);
        laserdst2 = new Rect(-100,-100,-100,-100);

        laserspeed = 16;
        lasery =-400;

        prevtime = System.currentTimeMillis();
        // endregion
        explode=Utils.loadImage(root,"images/exp2_0.png");
        explodesrc=new Rect();
        explodedst = new Rect();
        explodeframeno=0;
        exploded = false;


        kareno=0;

        animasyonno = 1;

        animasyonyonu = 0;

        hiz = 16;
        hizx = 0;
        hizy = 0;
        spritex = 0; //->Karakterin x düzlemindeki büyüklüğü
        spritey = 0; //->Karakterin y düzlemindeki büyüklüğü

        bulletspeed = 0;

        bulletspeedy = 0;

        bulletoffsetx_temp = 256;
        bulletoffsety_temp = 128;

        bulletx_temp = 0;
        bullety_temp = 0;

        bulletdst = new Vector<>();
        bulletx2 = new Vector<>();
        bullety2 = new Vector<>();
        bulletspeedx2 = new Vector<>();
        bulletspeedy2 = new Vector<>();
        bulletoffsetx2 = new Vector<>();
        bulletoffsety2 = new Vector<>();


    }


    public void update() {
        tilesrc.set(0,0,64,64);
        playsrc.set(0,0,256,256);

        playdst.set(getWidthHalf()-256 , getHeightHalf()-64 , getWidthHalf()-128 , getHeightHalf()+64);
        if (playshow)
        {


            return;      // Play butonuna basılmadan oyun baslamayacak
        }
        // sol üst sag alt


        restartsrc.set(256,0,512,256);
        restartdst.set(getWidthHalf()-64 , getHeightHalf()-64 , getWidthHalf()+64 , getHeightHalf()+64);
        exitsrc.set(512,0,768,256);
        exitdst.set(getWidthHalf()+128 , getHeightHalf()-64 , getWidthHalf()+256 , getHeightHalf()+64);




        lasersrc.set(0,0,64,128);



        laserdst1.set(laser1x,enemyy-128,enemyx+64,enemyy);
        laserdst2.set(laser2x ,enemyy-128,enemyx+256, enemyy );

        time = System.currentTimeMillis(); // robotun ateş sistemi için üretilen bir değer döndürür. Milisaniye cinsinden.
        if (time > prevtime+6000 && enemyexist) // milisn cinsinden oldugu icin 3000. 3 saniyede bir calısacak. Yani bu sürede lazer ekranın dısına cıkmazsa o lazeri silip  bastan atacaktır.
        // karakter mermisinde yaptıgımızı yapmamak icin ugrasmamak icin süreyi uzatıyoruz
        {
            prevtime=time;

            laser1x = enemyx;
            laser2x = enemyx+192;

            lasery = enemyy-100;
        }
        lasery-=laserspeed;
        // buradaki laser'lar ateslendikten sonraki noktaları belirlemek icin

        laserdst1.set(laser1x , lasery , laser1x+64, lasery+128);
        laserdst2.set(laser2x , lasery , laser2x+64 , lasery+128);

        if (spritedst.intersect(laserdst1) || spritedst.intersect(laserdst2))
        {
            spritedst.set(0,0,0,0);
            spriteexist=false;
            guishow=true;

        }

        //
        if (donmeboolean) // DONDURME
        {
            if (enemyspeedx >0)
            {
                donmenoktasi = enemyrnd.nextInt((getWidth()-256)-(enemyx+50)) + enemyx;
            }
            else if(enemyspeedx<0)
            {
                donmenoktasi = enemyrnd.nextInt(enemyx);

            }
            donmeboolean=false;
        }

        if (enemyspeedx>0 && enemyx>donmenoktasi) // Donme noktasına geldiginde donmeboolean tekrar true yapacaz burada. Bu sayede sürekli dönecek
        {
            donmeboolean=true;
            enemyspeedx=-enemyspeedx;
        }
        else if(enemyspeedx < 0 && enemyx<donmenoktasi)
        {
            donmeboolean=true;
            enemyspeedx=-enemyspeedx;

        }



        if (enemyexist) {
            enemysrc.set(0, 0, 64, 64);
            enemyfire=true;
            // sol ust sag alt
             //enemydst.set(getWidthHalf() - 128, getHeight() - 256, getWidthHalf() + 128, getHeight());
            enemydst.set(enemyx,enemyy,enemyx+256,enemyy+256);
        }

        for (int i = 0; i<bulletdst.size(); i++)
        {
            /*
            if(enemydst.intersect(bulletdst.elementAt(i)))// çarpışma için 2 rect birbirine değiyor mu  ?
            {


            }
            */
            if(enemydst.contains(bulletdst.elementAt(i)))
            {

               // Log.i("uyarı","KARDES NABIYON GAFAMA GELDI");
                explodedst.set(enemyx,enemyy,enemyx+256,enemyy+256);
                bulletx2.removeElementAt(i);
                bullety2.removeElementAt(i);
                bulletdst.removeElementAt(i);
                bulletspeedx2.removeElementAt(i);
                bulletspeedy2.removeElementAt(i);
                enemyexist=false;
                enemydst.set(0,0,0,0); // enemydst yi silmek icin. removeelement yok. Draw'da kontrol edecek.
                exploded=true;
                root.soundManager.play(sesefekti_patlama); // patlama sesi




            }
        }
        if (exploded)
        {
            explodesrc = getexplodeframe(explodeframeno);
            explodeframeno+=2; //  patlama efekti hızlı olsun diye 2 şer 2şer arttırıyoruz frameleri
        }

        if (explodeframeno>=15) // 2 şer 2 şer arttırdıgımızdan 14 -16 olacak ve bu ife girmeyecek == di >= yaptık
        {
            explodeframeno=0;
            exploded=false;
        }
        spritex += hizx;
        spritey += hizy;

        enemyx +=enemyspeedx;
        enemyy += enemyspeedy;

        if (enemyx + 256 > getWidth() || enemyx < 0)
        {
            enemyspeedx=-enemyspeedx;
        }


        for(int i=0; i < bulletx2.size(); i++)
        {
            bulletx2.set(i, bulletx2.elementAt(i) + bulletspeedx2.elementAt(i));//icindeki elemani degistirmeye calisiyoruz
            bullety2.set(i, bullety2.elementAt(i) + bulletspeedy2.elementAt(i));
            if(bulletx2.elementAt(i) > getWidth() || bulletx2.elementAt(i) < 0 || bullety2.elementAt(i) > getHeight() || bullety2.elementAt(i) < 0){
                bulletx2.removeElementAt(i);
                bullety2.removeElementAt(i);
                //bulletoffsety2.removeElementAt(i);
                //bulletoffsetx2.removeElementAt(i);
                bulletdst.removeElementAt(i);
                bulletspeedx2.removeElementAt(i);
                bulletspeedy2.removeElementAt(i);
            }
          //  Log.i("Control", String.valueOf(bulletx2.size()));
        }


        //bulletx_temp += bulletspeedx;
        //bullety_temp += bulletspeedy;

        if(spritex+256 > getWidth() || spritex < 0) {//x ekseni icin sona geldimi kontrolu
            hizx = 0;//spritex = getWidth() - 256;
            //animasyonno = 0;//sona gelince durma animasyonu
        }

        if(spritey+256 > getHeight() || spritey < 0){//y ekseni icin sona geldimi kontrolu
            hizy = 0;//spritey = getHeight() -256;
            //animasyonno = 0;//sona gelince durma animasyonu
        }

        if(animasyonno == 1)
            kareno++;
        else if(animasyonno == 0)
            kareno = 0;

        if(kareno > 8)
            kareno=1;

        if(hizx > 0)
            animasyonyonu = 0;
        else if(hizy > 0)
            animasyonyonu = 9;

        if(Math.abs(hizx) > 0 || Math.abs(hizy) > 0)
            animasyonno = 1;
        else {
            animasyonno = 0;
        }
            spritesrc.set(kareno * 128, animasyonyonu * 128, (kareno + 1) * 128, (animasyonyonu + 1) * 128);//Resimden aldigimiz koordinatlar
            if (spriteexist) {
                spritedst.set(spritex, spritey, spritex + 256, spritey + 256);//Ekrana cizilecegi koordinatlar
            }


            bulletsrc.set(0, 0, 70, 70);
            //bulletdst.set(bulletx_temp, bullety_temp, bulletx_temp + 32, bullety_temp + 32);

            for (int i = 0; i < bulletx2.size(); i++) {
                bulletdst.elementAt(i).set(bulletx2.elementAt(i), bullety2.elementAt(i), bulletx2.elementAt(i) + 32, bullety2.elementAt(i) + 32);
            }
        }




    public void draw(Canvas canvas) {
        //Log.i(TAG, "draw");
       /* for(int i=0; i < bulletx2.size(); i++)
        {
            bulletx2.set(i, bulletx2.elementAt(i) + bulletspeedx2.elementAt(i));//icindeki elemani degistirmeye calisiyoruz
            bullety2.set(i, bullety2.elementAt(i) + bulletspeedy2.elementAt(i));
            if(bulletx2.elementAt(i) > getWidth() || bulletx2.elementAt(i) < 0 || bullety2.elementAt(i) > getHeight() || bullety2.elementAt(i) < 0){

            }
        }*/



        for (int i=0; i<getWidth(); i+=128)
        {
            for(int j=0; j<getHeight(); j+=128)
            {
                tiledst.set(i,j,i+128,j+128);
                canvas.drawBitmap(tileset,tilesrc,tiledst,null);//yesil cimen zemini tum ekrana cizme
            }
        }
        canvas.drawBitmap(spritesheet,spritesrc,spritedst,null);

        for(int i = 0; i< bulletdst.size(); i++) {
            canvas.drawBitmap(bullet, bulletsrc, bulletdst.elementAt(i), null);
        }
        if (enemyexist)
        {
            canvas.drawBitmap(enemy, enemysrc, enemydst, null);
        }
        if (exploded)
        {
            canvas.drawBitmap(explode,explodesrc,explodedst,null);
        }

        canvas.drawBitmap(laser , lasersrc , laserdst1 , null);
        canvas.drawBitmap(laser , lasersrc , laserdst2 , null);

        if (guishow)
        {

            canvas.drawBitmap(buttons, restartsrc, restartdst,null);
            canvas.drawBitmap(buttons , exitsrc , exitdst, null);
        }
        if (playshow)
        {
            canvas.drawBitmap(buttons, playsrc, playdst,null);
        }

    }

    public void keyPressed(int key) {

    }

    public void keyReleased(int key) {

    }

    public boolean backPressed() {
        return true;
    }

    public void surfaceChanged(int width, int height) {

    }

    public void surfaceCreated() {

    }

    public void surfaceDestroyed() {

    }

    public void touchDown(int x, int y) {
        touchx = x;
        touchy = y;
    }

    public void touchMove(int x, int y) {
    }

    public void touchUp(int x, int y) {
        if((x - touchx) > 100)//saga cektiysek
        {
            animasyonno = 1;
            animasyonyonu = 0;

            hizx = hiz;
            hizy = 0;
        }
        else if((touchx - x) > 100)//sola cektiysek
        {
            animasyonno = 1;
            animasyonyonu = 1;

            hizx = -hiz;
            hizy = 0;
        }
        else if((y - touchy) > 100)//asagi cektiysek
        {
            animasyonno = 1;
            animasyonyonu = 9;

            hizy = hiz;
            hizx = 0;
        }
        else if((touchy - y) > 100)//yukari cektiysek
        {
            animasyonno = 1;
            animasyonyonu = 5;

            hizy = -hiz;
            hizx = 0;
        }


        else                    //mouse ile 100px den az bir degisim yaptiysak
        {
            animasyonno = 0;

            hizx = 0;
            hizy = 0;
            if (spriteexist) {    // karakter ölürse ates edememesi için
                bulletspeed = 32;

                if (animasyonyonu == 0) {

                    bulletspeedx2.add(bulletspeed);
                    bulletspeedy2.add(0);

                    bulletoffsetx_temp = 256;
                    bulletoffsety_temp = 128;
                } else if (animasyonyonu == 1) {
                    bulletspeedx2.add(-bulletspeed);
                    bulletspeedy2.add(0);

                    bulletoffsetx_temp = 0;
                    bulletoffsety_temp = 128;
                } else if (animasyonyonu == 9) {
                    bulletspeedy2.add(bulletspeed);
                    bulletspeedx2.add(0);

                    bulletoffsetx_temp = 128;
                    bulletoffsety_temp = 256;
                } else if (animasyonyonu == 5) {
                    bulletspeedy2.add(-bulletspeed);
                    bulletspeedx2.add(0);

                    bulletoffsetx_temp = 128;
                    bulletoffsety_temp = 0;
                }
                bulletx2.add(spritex + bulletoffsetx_temp);
                bullety2.add(spritey + bulletoffsety_temp);
                bulletx_temp = spritex + bulletoffsetx_temp;
                bullety_temp = spritey + bulletoffsety_temp;
                bulletdst.add(new Rect(bulletx_temp, bullety_temp, bulletx_temp + 32, bullety_temp + 32)); //diziye atadık her mermi bilgisini (mermi bilgisini en son ayarlarız)
            }
        }



        // Simdi buttonlar
        if (guishow) {

            if (restartdst.contains(x, y)) {
                Log.i(TAG, "RESTART'a tıklandı");
                root.setup();
            }
            if (exitdst.contains(x, y)) {
                Log.i(TAG, "EXİT'e tıklandı");
                System.exit(0);

            }
        }
        if (playshow) {
            if (playdst.contains(x, y)) {
                playshow = false;
            }
        }

        //
    }

    public Rect getexplodeframe(int frameno)
    {
        frameno=15-frameno; // effect tersten calıssın diye image'de
        Rect temp = new Rect();
        temp.set((frameno%4)*64,(frameno/4)*64,((frameno%4)+1)*64,((frameno/4)+1)*64);
        return temp;
    }

    public void pause() {

    }


    public void resume() {

    }


    public void reloadTextures() {

    }


    public void showNotify() {
    }

    public void hideNotify() {
    }

}
