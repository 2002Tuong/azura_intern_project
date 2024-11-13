package com.slideshowmaker.slideshow.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.adapter.StickerListAdapter
import com.slideshowmaker.slideshow.utils.DimenUtils
import kotlinx.android.synthetic.main.layout_view_choose_sticker.view.collectionView1
import kotlinx.android.synthetic.main.layout_view_choose_sticker.view.collectionView2
import kotlinx.android.synthetic.main.layout_view_choose_sticker.view.collectionView3
import kotlinx.android.synthetic.main.layout_view_choose_sticker.view.collectionView4
import kotlinx.android.synthetic.main.layout_view_choose_sticker.view.collectionView5
import kotlinx.android.synthetic.main.layout_view_choose_sticker.view.collectionView6
import kotlinx.android.synthetic.main.layout_view_choose_sticker.view.collectionView7
import kotlinx.android.synthetic.main.layout_view_choose_sticker.view.collectionView8
import kotlinx.android.synthetic.main.layout_view_choose_sticker.view.collectionView9
import kotlinx.android.synthetic.main.layout_view_choose_sticker.view.stickerListView

class ChooseStickerLayout : LinearLayout {

    private val stickerAdapter = StickerListAdapter {
        stickerCallback?.onSelectSticker(it)
    }

    private val defaultStickerPath = "file:///android_asset/"

    var stickerCallback: StickerCallback? = null

    constructor(context: Context?) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        inflate(context, R.layout.layout_view_choose_sticker, this)

        val col = DimenUtils.screenWidth(context) / (DimenUtils.density(context) * 56)
        stickerListView.adapter = stickerAdapter
        stickerListView.layoutManager = GridLayoutManager(context, 5)
        stickerAdapter.setItemList(getStickerCollection1())

        initActions()
    }

    private fun initActions() {
        collectionView1.setOnClickListener {
            stickerAdapter.setItemList(getStickerCollection1())
        }

        collectionView2.setOnClickListener {
            stickerAdapter.setItemList(getStickerCollection2())
        }

        collectionView3.setOnClickListener {
            stickerAdapter.setItemList(getStickerCollection3())
        }

        collectionView4.setOnClickListener {
            stickerAdapter.setItemList(getStickerCollection4())
        }

        collectionView5.setOnClickListener {
            stickerAdapter.setItemList(getStickerCollection5())
        }

        collectionView6.setOnClickListener {
            stickerAdapter.setItemList(getStickerCollection6())
        }

        collectionView7.setOnClickListener {
            stickerAdapter.setItemList(getStickerCollection7())
        }


        collectionView8.setOnClickListener {
            stickerAdapter.setItemList(getStickerCollection8())
        }

        collectionView9.setOnClickListener {
            stickerAdapter.setItemList(getStickerCollection9())
        }
    }

    private fun getStickerCollection1(): ArrayList<String> {

        val stickerList = ArrayList<String>()
        stickerList.add(R.drawable._4329937_0001_congrats.toString());
        stickerList.add(R.drawable._4329937_0002_it_is_your_day.toString());
        stickerList.add(R.drawable._4329937_0003_congratulations.toString());
        stickerList.add(R.drawable._4329937_0004_congratulations.toString());
        stickerList.add(R.drawable._4329937_0005_make_a_wish.toString());
        stickerList.add(R.drawable._4329937_0006_congrats.toString());
        stickerList.add(R.drawable._4329937_0007_congrats.toString());
        stickerList.add(R.drawable._4329937_0008_congrats.toString());
        stickerList.add(R.drawable._4329937_0009_well_done.toString());
        stickerList.add(R.drawable._4329937_0010_congratulations.toString());
        stickerList.add(R.drawable._4329937_0011_happy_wedding.toString());
        stickerList.add(R.drawable._4329937_0012_you_have_got_it.toString());
        stickerList.add(R.drawable._4329937_0013_best_wishes.toString());
        stickerList.add(R.drawable._4329937_0014_congrats.toString());
        stickerList.add(R.drawable._4329937_0015_wedding_day.toString());
        stickerList.add(R.drawable._4329937_0016_best_wishes.toString());
        stickerList.add(R.drawable._4329937_0017_congrats.toString());
        stickerList.add(R.drawable._4329937_0018_happy_birthday.toString());
        stickerList.add(R.drawable._4329937_0019_congrats.toString());
        stickerList.add(R.drawable._4329937_0020_i_am_proud_of_you.toString());



        return stickerList
    }

    private fun getStickerCollection2(): ArrayList<String> {
        val stickerList = ArrayList<String>()
        stickerList.add(R.drawable._4359605_0001_ferret.toString());
        stickerList.add(R.drawable._4359605_0002_turtle.toString());
        stickerList.add(R.drawable._4359605_0003_dog.toString());
        stickerList.add(R.drawable._4359605_0004_guinea_pig.toString());
        stickerList.add(R.drawable._4359605_0005_bunny.toString());
        stickerList.add(R.drawable._4359605_0006_bird.toString());
        stickerList.add(R.drawable._4359605_0007_dog.toString());
        stickerList.add(R.drawable._4359605_0008_hamster.toString());
        stickerList.add(R.drawable._4359605_0009_cat.toString());
        stickerList.add(R.drawable._4359605_0010_fish.toString());
        stickerList.add(R.drawable._4359605_0011_pig.toString());
        stickerList.add(R.drawable._4359605_0012_dog.toString());
        stickerList.add(R.drawable._4359605_0013_dog.toString());
        stickerList.add(R.drawable._4359605_0014_cat.toString());
        stickerList.add(R.drawable._4359605_0015_bird.toString());
        stickerList.add(R.drawable._4359605_0016_cat.toString());
        stickerList.add(R.drawable._4359605_0017_fish.toString());
        stickerList.add(R.drawable._4359605_0018_dog.toString());
        stickerList.add(R.drawable._4359605_0019_parrot.toString());
        stickerList.add(R.drawable._4359605_0020_mouse.toString());


        return stickerList
    }

    private fun getStickerCollection3(): ArrayList<String> {
        val stickerList = ArrayList<String>()
        stickerList.add(R.drawable._4359695_0001_snowman.toString());
        stickerList.add(R.drawable._4359695_0002_cookies.toString());
        stickerList.add(R.drawable._4359695_0003_merry_christmas.toString());
        stickerList.add(R.drawable._4359695_0004_merry_christmas.toString());
        stickerList.add(R.drawable._4359695_0005_elf.toString());
        stickerList.add(R.drawable._4359695_0006_cat.toString());
        stickerList.add(R.drawable._4359695_0007_holly.toString());
        stickerList.add(R.drawable._4359695_0008_holly.toString());
        stickerList.add(R.drawable._4359695_0009_santa_claus.toString());
        stickerList.add(R.drawable._4359695_0010_hot_chocolate.toString());
        stickerList.add(R.drawable._4359695_0011_reindeer.toString());
        stickerList.add(R.drawable._4359695_0012_snow.toString());
        stickerList.add(R.drawable._4359695_0013_merry_christmas.toString());
        stickerList.add(R.drawable._4359695_0014_christmas_tree.toString());
        stickerList.add(R.drawable._4359695_0015_ho_ho_ho.toString());
        stickerList.add(R.drawable._4359695_0016_mistletoe.toString());
        stickerList.add(R.drawable._4359695_0017_merry_christmas.toString());
        stickerList.add(R.drawable._4359695_0018_ho_ho_ho.toString());
        stickerList.add(R.drawable._4359695_0019_christmas_sock.toString());
        stickerList.add(R.drawable._4359695_0020_cookie.toString());


        return stickerList
    }

    private fun getStickerCollection4(): ArrayList<String> {
        val stickerList = ArrayList<String>()
        stickerList.add(R.drawable._4392451_0001_chili_pepper.toString());
        stickerList.add(R.drawable._4392451_0002_bell_pepper.toString());
        stickerList.add(R.drawable._4392451_0003_carrot.toString());
        stickerList.add(R.drawable._4392451_0004_strawberry.toString());
        stickerList.add(R.drawable._4392451_0005_tomato.toString());
        stickerList.add(R.drawable._4392451_0006_fruit.toString());
        stickerList.add(R.drawable._4392451_0007_pear.toString());
        stickerList.add(R.drawable._4392451_0008_watermelon.toString());
        stickerList.add(R.drawable._4392451_0009_eat.toString());
        stickerList.add(R.drawable._4392451_0010_radish.toString());
        stickerList.add(R.drawable._4392451_0011_eggplant.toString());
        stickerList.add(R.drawable._4392451_0012_vegan.toString());
        stickerList.add(R.drawable._4392451_0013_doughnut.toString());
        stickerList.add(R.drawable._4392451_0014_pizza.toString());
        stickerList.add(R.drawable._4392451_0015_fast_food.toString());
        stickerList.add(R.drawable._4392451_0016_apple.toString());
        stickerList.add(R.drawable._4392451_0017_tomato.toString());
        stickerList.add(R.drawable._4392451_0018_pineapple.toString());
        stickerList.add(R.drawable._4392451_0019_radish.toString());
        stickerList.add(R.drawable._4392451_0020_tomatoes.toString());


        return stickerList
    }

    private fun getStickerCollection5(): ArrayList<String> {
        val stickerList = ArrayList<String>()

        stickerList.add(R.drawable._4433167_0001_summer.toString());
        stickerList.add(R.drawable._4433167_0002_parrot.toString());
        stickerList.add(R.drawable._4433167_0003_coconut_drink.toString());
        stickerList.add(R.drawable._4433167_0004_beach_ball.toString());
        stickerList.add(R.drawable._4433167_0005_turtle.toString());
        stickerList.add(R.drawable._4433167_0006_flip_flops.toString());
        stickerList.add(R.drawable._4433167_0007_surfboard.toString());
        stickerList.add(R.drawable._4433167_0008_photo_camera.toString());
        stickerList.add(R.drawable._4433167_0009_popsicle.toString());
        stickerList.add(R.drawable._4433167_0010_cocktail.toString());
        stickerList.add(R.drawable._4433167_0011_sun.toString());
        stickerList.add(R.drawable._4433167_0012_rubber_ring.toString());
        stickerList.add(R.drawable._4433167_0013_rubber_ring.toString());
        stickerList.add(R.drawable._4433167_0014_swimsuit.toString());
        stickerList.add(R.drawable._4433167_0015_fish.toString());
        stickerList.add(R.drawable._4433167_0016_roller_skates.toString());
        stickerList.add(R.drawable._4433167_0017_crab.toString());
        stickerList.add(R.drawable._4433167_0018_bikini.toString());
        stickerList.add(R.drawable._4433167_0019_ice_cream_cone.toString());
        stickerList.add(R.drawable._4433167_0020_watermelon.toString());


        return stickerList
    }

    private fun getStickerCollection6(): ArrayList<String> {
        val stickerList = ArrayList<String>()

        stickerList.add(R.drawable._4471019_0001_merry_and_bright.toString());
        stickerList.add(R.drawable._4471019_0002_joy_to_the_world.toString());
        stickerList.add(R.drawable._4471019_0003_happy_new_year.toString());
        stickerList.add(R.drawable._4471019_0004_its_your_day.toString());
        stickerList.add(R.drawable._4471019_0005_im_glad_you_were_born.toString());
        stickerList.add(R.drawable._4471019_0006_hughs_and_kisses.toString());
        stickerList.add(R.drawable._4471019_0007_its_a_party.toString());
        stickerList.add(R.drawable._4471019_0008_happy_anniversary.toString());
        stickerList.add(R.drawable._4471019_0009_have_the_best_day.toString());
        stickerList.add(R.drawable._4471019_0010_wishing_you_well.toString());
        stickerList.add(R.drawable._4471019_0011_time_to_celebrate.toString());
        stickerList.add(R.drawable._4471019_0012_you_did_it.toString());
        stickerList.add(R.drawable._4471019_0013_yip_yip.toString());
        stickerList.add(R.drawable._4471019_0014_yay_for_cake.toString());
        stickerList.add(R.drawable._4471019_0015_congrats.toString());
        stickerList.add(R.drawable._4471019_0016_cheers.toString());
        stickerList.add(R.drawable._4471019_0017_make_a_wish.toString());
        stickerList.add(R.drawable._4471019_0018_best_wishes.toString());
        stickerList.add(R.drawable._4471019_0019_happy_birthday.toString());
        stickerList.add(R.drawable._4471019_0020_seasons_greetings.toString());

        return stickerList
    }

    private fun getStickerCollection7(): ArrayList<String> {
        val stickers = ArrayList<String>()
        stickers.add(R.drawable._4850762_0001_love_message.toString());
        stickers.add(R.drawable._4850762_0002_business_strategy.toString());
        stickers.add(R.drawable._4850762_0003_best_employee.toString());
        stickers.add(R.drawable._4850762_0004_working_hours.toString());
        stickers.add(R.drawable._4850762_0005_working.toString());
        stickers.add(R.drawable._4850762_0006_pie_chart.toString());
        stickers.add(R.drawable._4850762_0007_marketing.toString());
        stickers.add(R.drawable._4850762_0008_busy.toString());
        stickers.add(R.drawable._4850762_0009_working.toString());
        stickers.add(R.drawable._4850762_0010_chess_game.toString());
        stickers.add(R.drawable._4850762_0011_search.toString());
        stickers.add(R.drawable._4850762_0012_target.toString());
        stickers.add(R.drawable._4850762_0013_puzzle.toString());
        stickers.add(R.drawable._4850762_0014_business_idea.toString());
        stickers.add(R.drawable._4850762_0015_business.toString());
        stickers.add(R.drawable._4850762_0016_management.toString());
        stickers.add(R.drawable._4850762_0017_documentation.toString());
        stickers.add(R.drawable._4850762_0018_trophy.toString());
        stickers.add(R.drawable._4850762_0019_analysis.toString());
        stickers.add(R.drawable._4850762_0020_growth.toString());



        return stickers
    }

    private fun getStickerCollection8(): ArrayList<String> {
        val stickers = ArrayList<String>()
        stickers.add(R.drawable._5020346_0008_chocolate_bar.toString());
        stickers.add(R.drawable._5020346_0001_award.toString());
        stickers.add(R.drawable._5020346_0002_beer.toString());
        stickers.add(R.drawable._5020346_0003_broken_heart.toString());
        stickers.add(R.drawable._5020346_0004_tired.toString());
        stickers.add(R.drawable._5020346_0005_celebration.toString());
        stickers.add(R.drawable._5020346_0006_cauldron.toString());
        stickers.add(R.drawable._5020346_0007_celebration.toString());
        stickers.add(R.drawable._5020346_0009_computer.toString());
        stickers.add(R.drawable._5020346_0010_fishing.toString());
        stickers.add(R.drawable._5020346_0011_graduation.toString());
        stickers.add(R.drawable._5020346_0012_happy.toString());
        stickers.add(R.drawable._5020346_0013_superhero.toString());
        stickers.add(R.drawable._5020346_0014_hidden.toString());
        stickers.add(R.drawable._5020346_0015_idea.toString());
        stickers.add(R.drawable._5020346_0016_vacation.toString());
        stickers.add(R.drawable._5020346_0017_workout.toString());
        stickers.add(R.drawable._5020346_0018_read.toString());
        stickers.add(R.drawable._5020346_0019_love.toString());
        stickers.add(R.drawable._5020346_0020_chemistry.toString());
        stickers.add(R.drawable._5020346_0021_meditation.toString());
        stickers.add(R.drawable._5020346_0022_mind_blown.toString());
        stickers.add(R.drawable._5020346_0023_investment.toString());
        stickers.add(R.drawable._5020346_0024_question.toString());
        stickers.add(R.drawable._5020346_0025_rainbow.toString());
        stickers.add(R.drawable._5020346_0026_rich.toString());
        stickers.add(R.drawable._5020346_0027_salad.toString());
        stickers.add(R.drawable._5020346_0028_selfie.toString());
        stickers.add(R.drawable._5020346_0029_sexy.toString());
        stickers.add(R.drawable._5020346_0030_sick.toString());
        stickers.add(R.drawable._5020346_0031_happy.toString());
        stickers.add(R.drawable._5020346_0032_goal.toString());
        stickers.add(R.drawable._5020346_0033_winner.toString());
        stickers.add(R.drawable._5020346_0034_hello.toString());
        stickers.add(R.drawable._5020346_0035_stress.toString());
        stickers.add(R.drawable._5020346_0036_workout.toString());

        return stickers
    }

    private fun getStickerCollection9(): ArrayList<String> {
        val sticker = ArrayList<String>()
        sticker.add(R.drawable._5722287_0001_cat_animal.toString());
        sticker.add(R.drawable._5722287_0002_cat_animal.toString());
        sticker.add(R.drawable._5722287_0003_cat_animal.toString());
        sticker.add(R.drawable._5722287_0004_cat_animal.toString());
        sticker.add(R.drawable._5722287_0005_cat_animal.toString());
        sticker.add(R.drawable._5722287_0006_cat_animal.toString());
        sticker.add(R.drawable._5722287_0007_cat_animal.toString());
        sticker.add(R.drawable._5722287_0008_cat_animal.toString());
        sticker.add(R.drawable._5722287_0009_cat_animal.toString());
        sticker.add(R.drawable._5722287_0010_cat_animal.toString());
        sticker.add(R.drawable._5722287_0011_cat_animal.toString());
        sticker.add(R.drawable._5722287_0012_cat_animal.toString());
        sticker.add(R.drawable._5722287_0013_cat_animal.toString());
        sticker.add(R.drawable._5722287_0014_cat_animal.toString());
        sticker.add(R.drawable._5722287_0015_cat_animal.toString());
        sticker.add(R.drawable._5722287_0016_cat_animal.toString());
        sticker.add(R.drawable._5722287_0017_cat_animal.toString());
        sticker.add(R.drawable._5722287_0018_cat_animal.toString());
        sticker.add(R.drawable._5722287_0019_cat_animal.toString());
        sticker.add(R.drawable._5722287_0020_cat_animal.toString());


        return sticker
    }

    private fun getAllFilePathInAsset(folderName: String): ArrayList<String> {
        val filePathList = ArrayList<String>()
        val imageName = context.resources.assets.list(folderName) ?: arrayOf()
        for (image in imageName) {
            filePathList.add("$defaultStickerPath$folderName/$image")
        }
        return filePathList
    }

    interface StickerCallback {
        fun onSelectSticker(stickerPath: String)
    }

}