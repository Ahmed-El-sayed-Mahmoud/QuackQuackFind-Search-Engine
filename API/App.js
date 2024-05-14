const mongoose=require("./Connection/ConnectDataBase")
const URL=require("./Schema/UrlSchema")
const Word=require("./Schema/WordsSchema")
const URLRouter=require("./Router/UrlRouter")
const WordRouter=require("./Router/WordsRouter")
const RecommenderRouter=require("./Router/RecommenderRouter")
const cors = require('cors');
const express=require("express")
const app=express()
app.use(cors());
app.use(express.json())
app.use("/URL",URLRouter)
app.use("/Word",WordRouter)
app.use("/api",RecommenderRouter)

app.listen(3000,()=>{
    console.log("connected at port 3000")

})




