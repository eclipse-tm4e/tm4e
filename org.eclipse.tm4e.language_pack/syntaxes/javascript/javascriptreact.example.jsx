import React, { useState, useEffect } from 'react';

// Functional Component
const ExampleComponent = ({ name }) => {
  // State and Effect Hook
  const [count, setCount] = useState(0);

  useEffect(() => {
    console.log('Component is mounted or count is updated');
    return () => {
      console.log('Component will unmount');
    };
  }, [count]);

  // JSX Elements
  return (
    <div>
      <h1>Hello, {name}!</h1>
      <p>Count: {count}</p>

      {/* Conditional Rendering */}
      {count > 5 && <p>Count is greater than 5.</p>}

      {/* Event Handling */}
      <button onClick={() => setCount(count + 1)}>Increment Count</button>

      {/* JSX Fragments */}
      <>
        <p>Fragment Example</p>
        <p>Another Fragment Element</p>
      </>

      {/* Array Mapping */}
      {[1, 2, 3].map((num) => (
        <span key={num}>{num}</span>
      ))}
    </div>
  );
};

// JSX Component Composition
const App = () => {
  return (
    <>
      <ExampleComponent name="Alice" />
      <ExampleComponent name="Bob" />
    </>
  );
};

export default App;
