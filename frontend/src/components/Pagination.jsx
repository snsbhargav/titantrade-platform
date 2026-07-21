function Pagination({page, last, totalPages, handlePrev, handleNext}){

    return (
        <>
        <div className="pagination">
            <button name="prev" onClick={handlePrev} disabled={page===0}>Prev</button>
            <button name="next" onClick={handleNext} disabled={last}>Next</button>
        </div>
        <div className="pagination-tail">
            <p >Page {page+1} of {totalPages}</p>
        </div>
        </>
    );
}

export default Pagination;