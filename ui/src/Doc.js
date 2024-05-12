import React from 'react';

const Doc = ({ doc }) => {
  return (
    <div className="doc-container">
      <a href={doc["Url"]} target="_blank" rel="noopener noreferrer"><h3>{doc["Title"]}</h3></a>
      <span dangerouslySetInnerHTML={{ __html: doc["Describtion"] }}></span>
    </div>
  );
};

export default Doc;
