const dotenv=require("dotenv").config()
const mongoose=require("mongoose")
mongoose.connect(process.env.ConnectionUrl)
module.exports=mongoose