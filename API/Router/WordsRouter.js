const express=require("express")
const WordsController=require("../Controller/WordsController")
const WordsMiddleWare=require("../MiddelWare/WordsMiddeleWare")
const router=express.Router()
router.post("/Insert",WordsMiddleWare.CheckData,WordsController.FinalInsert)
router.delete("/Delete",WordsMiddleWare.ExistMiddleWare,WordsController.DeleteWordInaURL)
//router.patch("/UpdateIDF",WordsController.UpdateIDF)//FOR ALL DATABASE WHEN INSERT ALL WORDS OF NEW LINK OR DELETE EXISTED LINK
router.get("/Get/:word",WordsMiddleWare.ExistMiddleWare,WordsController.GetWordInfo)
module.exports=router