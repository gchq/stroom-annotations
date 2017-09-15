import React from 'react';
import { connect } from 'react-redux'

import { updateAnnotation } from '../actions/updateAnnotation';
import { removeAnnotation } from '../actions/removeAnnotation';

import SelectStatus from './SelectStatus';

import RaisedButton from 'material-ui/RaisedButton';
import TextField from 'material-ui/TextField';

export const EditAnnotation = (props) => {

    const onContentChange = (e) => {
        const annotation = {
            content: e.target.value,
            status: props.annotation.status
        }

        props.updateAnnotation(props.annotation.id, annotation)
    }

    const onStatusChange = (e) => {
        const annotation = {
            content: props.annotation.content,
            status: e.target.value
        }

        props.updateAnnotation(props.annotation.id, annotation)
    }

    return (
        <div>
            <TextField value={props.annotation.content} onChange={onContentChange}
                    hintText="Write notes against this event"
                    floatingLabelText="Annotation Content"
                    multiLine={true}
                    rows={2}
                    rowsMax={4}
                />
            <br />

            <SelectStatus value={props.annotation.status} onChange={onStatusChange} />
            <br />

            <RaisedButton label='Remove Annotation' onClick={() => props.removeAnnotation(props.annotation.id)} />
        </div>
    )
}

export default connect(
  (state) => ({
     annotation: state.annotation.annotation
  }),
  {
     updateAnnotation,
     removeAnnotation
  }
)(EditAnnotation)
