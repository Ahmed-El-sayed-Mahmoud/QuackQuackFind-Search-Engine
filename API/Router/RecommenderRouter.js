const express=require("express")
const SearchController=require("../Controller/RecommenderController")
const router=express.Router()
router.post("/insert",SearchController.insertOrUpdateQuery)
router.get("/get/:word",SearchController.getQueriesContainingInput);

module.exports=router