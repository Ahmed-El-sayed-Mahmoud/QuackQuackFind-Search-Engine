import React, { useState, useEffect } from "react";
import "./SearchPage.css";
import Doc from "./Doc";
import clickSound from "./media/quack.mp3";
import angryDuck from "./media/angry_duck.jpg";
import runningDuck from "./media/running_duck.jpeg";
import sadDuck from "./media/sad_duck.png";
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
let SearchResult=new Map();
const SearchPage = ({ onSearch }) => {
 
  const [searchResults, setSearchResults] = useState([]);
  const [input, setinput] = useState("");
  const [shouldUpdateResults, setShouldUpdateResults] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [tenSearchResult, setTenSearchResult] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTime, setSearchTime] = useState(0);
  const [initialSearch, setinitialSearch] = useState(true);
  const handleSearch = async () => {
    if (input.trim() !== "") {
      setLoading(true);
      const startTime = performance.now(); // Start timing the search operation
      try {
        const response = await fetch(`http://localhost:8090/search/${input}`);
        if (response.ok) {
          const data = await response.json();
          SearchResult.set(1, data.slice(0, 10));
          setSearchResults(data);
          setTenSearchResult(data.slice(0, 10));
          setShouldUpdateResults(true);
          setCurrentPage(1);
        } else {
          setinitialSearch(false);
          setShouldUpdateResults(false);
          console.error("Error fetching search results:", response.statusText);
        }
      } catch (error) {
        setShouldUpdateResults(false);
        console.error("Error fetching search results:", error);
      }
      setLoading(false);
      const endTime = performance.now(); // End timing the search operation
      setSearchTime(endTime - startTime); // Calculate and set the search time
    }
  };

  useEffect(() => {
    handleSearch(); // Trigger initial search
  }, []);

  function ChangePage(value) {
    if (value < 1) {
      value = 1;
    }
    setCurrentPage(value);
    setLoading(true);
    window.scrollTo({ top: 0, behavior: "smooth" });
    const searchData = {
      NormalQuery: input,
      docs: searchResults.slice((value - 1) * 10, value * 10),
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
        if (data.length === 0) {
          setShouldUpdateResults(false);
        } else {
          setTenSearchResult(data);
          SearchResult.set(value, data);
          setShouldUpdateResults(true);
        }
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error:", error);
        setLoading(false);
        // Handle errors here
      });
  }

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
              onChange={(e) => setinput(e.target.value)}
            />
            <button className="search-button" onClick={handleSearch}>
              Search
            </button>
          </div>
        )}
      </div>
    </header>
    {!loading && ( // Render search results only if not loading
    <div>
       <div className="search-time">
        <h3>Search took: {searchTime.toFixed(2)} milliseconds</h3>
        </div>
      <div className="search-results">
        {shouldUpdateResults ||initialSearch ?  (
          tenSearchResult.map((doc, index) => (
            <div className="search-result" key={index}>
              <Doc doc={doc} />
            </div>
          ))
        ) : (
          <div className="not-found-container">
          <h1 className="not-found-message">No results found</h1>
          <img className="image-notfound" src={sadDuck} alt="sadDuck" />
          </div>
          )
        }
      </div>
      </div>
    )}
   {shouldUpdateResults && tenSearchResult.length > 0 && ( // Only render page numbers if there are search results
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
          backgroundColor: "lightblue"
        }}
         onClick={() => ChangePage(currentPage)}>{currentPage}</button>
      </>
    )}
    <button onClick={() => ChangePage(currentPage + 1)}>next</button>
  </div>
)}
  </>

  );
};

export default SearchPage;
