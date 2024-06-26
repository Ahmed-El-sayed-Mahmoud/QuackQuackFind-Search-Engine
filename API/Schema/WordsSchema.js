const mongoose = require("mongoose")
const Word = new mongoose.Schema({
    Word: {
        type: String,
        required: true,
        lowercase: true
    },
    Urls: [{
        Url: {
            // type: mongoose.SchemaTypes.ObjectId,
            // ref:"URL"
           type: String,
           
        },
        Title:String
        ,
        NumofOccure: {
            default: 0,
            type: Number
        },
        TF:mongoose.Schema.Types.Decimal128,
        Rank:Number

    }],
   // IDF:mongoose.Schema.Types.Decimal128,

   
})
module.exports = mongoose.model("Word", Word)