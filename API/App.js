const mongoose=require("./Connection/ConnectDataBase")
const URL=require("./Schema/UrlSchema")
const Word=require("./Schema/WordsSchema")
const URLRouter=require("./Router/UrlRouter")
const WordRouter=require("./Router/WordsRouter")

const express=require("express")
const app=express()
app.use(express.json())
app.use("/URL",URLRouter)
app.use("/Word",WordRouter)



app.listen(3000,()=>{
    console.log("connected")

})

