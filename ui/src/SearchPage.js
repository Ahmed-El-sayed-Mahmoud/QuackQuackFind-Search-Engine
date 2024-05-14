import React, { useState, useEffect } from "react";
import "./SearchPage.css";
import Doc from "./Doc";
import clickSound from "./media/quack.mp3";
import angryDuck from "./media/file.png";
import runningDuck from "./media/pato-duck.gif";
import sadDuck from "./media/sad_duck.png"
import {asInterruptible, InterruptError} from 'interruptible'
const ClickSound = () => {
  useEffect(() => {
    // Function to play the sound
    const playClickSound = () => {
      const audio = new Audio(clickSound);
      audio.play();
    };

    // Add click event listener to the document
    document.addEventListener("click", playClickSound);

    // Cleanup function to remove the event listener when the component unmounts
    return () => {
      document.removeEventListener("click", playClickSound);
    };
  }, []); // Empty dependency array ensures the effect runs only once when the component mounts

  return null; // Since this component is only for side effects, return null
};
async function postData() {
  const data = {
    query: GlobalInput,
  };
  const response = await fetch("http://localhost:3000/api/insert/", {
    method: 'POST', // *GET, POST, PUT, DELETE, etc.
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data) // body data type must match "Content-Type" header
  });
  return response.json(); // parses JSON response into native JavaScript objects
}
let SearchResult=new Map();
let GlobalInput;
let GlobalSearchResultS=[];
let u=0;
let tenSearchResult=[];
function  fetchData() {
  //console.log(GlobalSearchResultS)
  for(let i=2;i<=((GlobalSearchResultS.length)/10);i++)
    {
      if(GlobalSearchResultS.slice((i-1)*10, (i-1) * 10 + 10).length==0)
        return;
      console.log((i-1)*10," ",(i-1) * 10 + 10);
        const searchData = {
          NormalQuery: GlobalInput,
          docs: GlobalSearchResultS.slice((i-1)*10, (i-1) * 10 + 10),
        };
        console.log(searchData)
        fetch("http://localhost:8090/api", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(searchData),
          })
            .then((response) => response.json())
            .then((data) => {
                SearchResult.set(i,data);
                console.log(SearchResult);
            })
            .catch((error) => {
              console.error("Error:", error);
            });
    }
  
}
const SearchPage = ({ onSearch }) => {
 
  const [searchResults, setSearchResults] = useState([]);
  const [input, setinput] = useState("");
  const [shouldUpdateResults, setShouldUpdateResults] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [searchTime, setSearchTime] = useState(0);
  const [initialSearch, setinitialSearch] = useState(true);
  const [recommendedResults, setRecommendedResults] = useState([]);
  const [UpdateRecommend,setUpdateRecommend]=useState(false);
  const handleInputChange = async (e) => {
    const inputValue = e.target.value;
    setinput(inputValue);

    try {
      const response = await fetch(`http://localhost:3000/api/get/${inputValue}`);
      if (response.ok) {
        const data = await response.json();
        setRecommendedResults(data);
        console.log(recommendedResults);
        setUpdateRecommend(true);
      } else {
        console.error('Error fetching recommendations:', response.statusText);
      }
    } catch (error) {
      console.error('Error fetching recommendations:', error);
    }
  };
  const handleSearch = async () => {
    SearchResult.clear();
    if (input.trim() !== "") {
      setLoading(true);
      const startTime = performance.now(); // Start timing the search operation
      try {
        const response = await fetch(`http://localhost:8090/search/${input}`);
        if (response.ok) {
          const data = await response.json();

          if(input[0]=='"')
          {
            console.log("phrase");
            tenSearchResult=(data.slice(0, 20));
            SearchResult.set(1,data.slice(0,20));
            console.log(SearchResult);
            GlobalSearchResultS=data;
            console.log(tenSearchResult)
            setShouldUpdateResults(true);
            console.log(shouldUpdateResults)
            console.log(u)
            console.log("after")
          }
          else
          {
            SearchResult.set(1,data.slice(0,10));
            console.log(SearchResult);
            setSearchResults(data);
            GlobalSearchResultS=data;
            tenSearchResult=(data.slice(0, 10));
            setShouldUpdateResults(true);
          } 
          
        } else {
          console.error("Error fetching search results:", response.statusText);
        }
      } catch (error) {
        console.error("Error fetching search results:", error);
      }
      setLoading(false);
      const endTime = performance.now(); // End timing the search operation
      setSearchTime(endTime - startTime); // Calculate and set the search time
      GlobalInput=input;
    }
    console.log(GlobalInput)
    fetchData();
    postData();
  };


  function ChangePage(value) {
    //setLoading(true);
    window.scrollTo({ top: 0, behavior: "smooth" });
    setCurrentPage(value);
    console.log(value);
    if(SearchResult.has(value))
      {
        console.log("changing to known",value);
        tenSearchResult=(SearchResult.get(value));
        setShouldUpdateResults(true);
        setLoading(false);
      }
      else
      {
        const searchData = {
          NormalQuery: input,
          docs: searchResults.slice(value * 10, value * 10 + 10),
        };
    
        fetch("http://localhost:8090/api", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(searchData),
        })
          .then((response) => response.json())
          .then((data) => {
            tenSearchResult=(data);
            SearchResult.set(value,data);
            console.log(SearchResult);
            setShouldUpdateResults(true);
            setLoading(false);
          })
          .catch((error) => {
            console.error("Error:", error);
            setLoading(false);
            // Handle errors here
          });
          setShouldUpdateResults(true);
      }
    
  }
  const handleResultClick = (result) => {
    //setSelectedResult(result);
    setinput(result["query"]);
  };

  return (
    <>
  <ClickSound />
  <header className="search_header">
    <div className="header-container">
      <h1 className="header-title">Quack Quack Find</h1>
      {!loading && (
        <div className="image-container">
          <img className="image" src={angryDuck} alt="Running Duck" />
        </div>
      )}
      {loading ? (
        <div className="loading-container">
          <div className="image-container">
            <img className="image" src={runningDuck} alt="Running Duck" />
          </div>
          <h1>Loading...</h1>
        </div>
      ) : (
        <div className="search-container">
          <input
            className="search-input"
            type="text"
            placeholder="Search..."
            value={input}
            onChange={handleInputChange}
          />
          <button className="search-button" onClick={handleSearch}>
            Search
          </button>
          {recommendedResults.length > 0 && (
        <ul>
          {UpdateRecommend&&recommendedResults.map((result, index) => (
            <li key={index} onClick={() => handleResultClick(result)}>{result["query"]}</li>
          ))}
        </ul>
      )}
        </div>
      )}
    </div>
  </header>
  {!loading && (
    <div>
      <div className="search-time">
        <h3>Search took: {searchTime.toFixed(2)} milliseconds</h3>
      </div>
      <div className="search-results">
        {shouldUpdateResults ? (
          tenSearchResult.length > 0 ? (
            tenSearchResult.map((doc, index) => (doc["Show"]&&(doc["Describtion"]!=null)&&<Doc key ={index}doc={doc}/>))
          ) : (
            <div className="not-found-container">
              <h1 className="not-found-message">No results found</h1>
            </div>
          )
        ) : null}
      </div>
    </div>
  )}
  {shouldUpdateResults && tenSearchResult.length > 0 && (
    <div className="page-numbers-container">
      <button onClick={() => ChangePage(currentPage - 1)}>prev</button>
      {[1, 2, 3, 4, 5, 6, 7].map((pageNumber) => (
        <button
          key={pageNumber}
          onClick={() => ChangePage(pageNumber)}
          style={{
            backgroundColor: currentPage === pageNumber ? "lightblue" : "#de5833",
          }}
        >
          {pageNumber}
        </button>
      ))}
      {currentPage > 7 && (
        <>
          <button
            style={{
              backgroundColor: "lightblue",
            }}
            onClick={() => ChangePage(currentPage)}
          >
            {currentPage}
          </button>
        </>
      )}
      <button onClick={() => ChangePage(currentPage + 1)}>next</button>
    </div>
  )}
</>

  );
};

export default SearchPage;