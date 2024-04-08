const mongoose = require("mongoose")
const Url = new mongoose.Schema({
    URL: {
        type: String,
        required: true
    },
    Title: {
        type: String,
        required: true
    },
    Rank: {
        type: Number,
        required: true
    },
    NumberofWords: {
        type: Number,
        required: true
    }
    
})
module.exports = mongoose.model("URL", Url)
