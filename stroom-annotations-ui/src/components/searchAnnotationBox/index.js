import React from 'react';
import { connect } from 'react-redux'

import ActionSearch from 'material-ui/svg-icons/action/search';
import TextField from 'material-ui/TextField';

import { searchAnnotations } from '../../actions/searchAnnotations';

const iconStyles = {
  marginRight: "1rem",
};

let SearchAnnotationBox = (props) => {
    const onSearchTermChange = (e) => {
        props.searchAnnotations(e.target.value);
    }

    return (
        <div>
            <ActionSearch style={iconStyles} />
            <TextField
                value={props.searchTerm} onChange={onSearchTermChange}
                  hintText="Content, AssignTo or ID"
                  floatingLabelText="Search for Annotations" />
        </div>
    )
}

export default connect(
    (state) => ({
        searchTerm: state.annotations.searchTerm
    }),
    {
        searchAnnotations
    }
)(SearchAnnotationBox)
