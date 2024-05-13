import React, { useState, useEffect } from "react";
import "./SearchPage.css";
import Doc from "./Doc";
import clickSound from "./media/quack.mp3";
import angryDuck from "./media/angry_duck.jpg";
import runningDuck from "./media/running_duck.jpeg";

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
  const [shouldUpdateResults, setShouldUpdateResults] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [tenSearchResult, setTenSearchResult] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleSearch = async () => {
    if (input.trim() !== "") {
      setLoading(true);
      try {
        const response = await fetch(`http://localhost:8090/search/${input}`);
        if (response.ok) {
          const data = await response.json();
          SearchResult.set(1,data.slice(0,10));
          console.log(SearchResult);
          setSearchResults(data);
          setTenSearchResult(data.slice(0, 10));
          setShouldUpdateResults(true);
        } else {
          console.error("Error fetching search results:", response.statusText);
        }
      } catch (error) {
        console.error("Error fetching search results:", error);
      }
      setLoading(false);
    }
  };

  useEffect(() => {
    handleSearch(); // Trigger initial search
  }, []);

  function ChangePage(value) {
    setLoading(true);
    window.scrollTo({ top: 0, behavior: "smooth" });
    setCurrentPage(value);
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
        setTenSearchResult(data);
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
        <div className="search-results">
          {shouldUpdateResults &&
            tenSearchResult.map((doc, index) => (
              <div className="search-result">
                <Doc key={index} doc={doc} />
              </div>
            ))}
        </div>
      )}
      <div className="page-numbers-container">
        <button onClick={() => ChangePage(2)}>2</button>
        <button onClick={() => ChangePage(3)}>3</button>
        <button onClick={() => ChangePage(4)}>4</button>
        <button onClick={() => ChangePage(5)}>5</button>
        <button onClick={() => ChangePage(6)}>6</button>
        <button onClick={() => ChangePage(7)}>7</button>
      </div>
    </>
  );
};

export default SearchPage;
