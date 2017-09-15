import React, { Component } from 'react';
import { connect } from 'react-redux'

import { updateAnnotation } from '../../actions/updateAnnotation';
import { removeAnnotation } from '../../actions/removeAnnotation';

import './EditAnnotation.css'

import SelectStatus from '../selectStatus';

import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';
import RaisedButton from 'material-ui/RaisedButton';
import TextField from 'material-ui/TextField';

export class EditAnnotation extends Component {

    constructor(props) {
        super(props);

        this.state = {
            open: false,
        };
    }

    handleOpen() {
        this.setState({open: true});
    };

    handleClose() {
        this.setState({open: false});
    };

    handleRemoveAndClose() {
        this.props.removeAnnotation(this.props.annotation.id)
        this.handleClose();
    }

    onContentChange (e) {
        const annotation = {
            content: e.target.value,
            status: this.props.annotation.status
        }

        this.props.updateAnnotation(this.props.annotation.id, annotation)
    }

    onStatusChange (e) {
        const annotation = {
            content: this.props.annotation.content,
            status: e.target.value
        }

        this.props.updateAnnotation(this.props.annotation.id, annotation)
    }

    render() {
        const actions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onClick={this.handleClose.bind(this)}
                />,
            <FlatButton
                label="Remove"
                primary={true}
                onClick={this.handleRemoveAndClose.bind(this)}
                />,
        ];

        const customContentStyle = {
            width: '20rem',
            maxWidth: 'none',
        };

        return (
            <div className='edit-annotation'>
                <TextField value={this.props.annotation.content} onChange={this.onContentChange.bind(this)}
                        hintText="Write notes against this event"
                        floatingLabelText="Annotation Content"
                        multiLine={true}
                        rows={2}
                        rowsMax={4}
                        fullWidth={true}
                    />

                <SelectStatus
                        value={this.props.annotation.status}
                        onChange={this.onStatusChange.bind(this)}
                        />

                <RaisedButton
                        label="Remove Annotation"
                        onClick={this.handleOpen.bind(this)}
                        className='edit-annotation__remove-button'
                        />
                <Dialog
                  actions={actions}
                  modal={false}
                  open={this.state.open}
                  onRequestClose={this.handleClose.bind(this)}
                  contentStyle={customContentStyle}
                >
                    Remove the Annotation for event {this.props.annotation.id}?
                </Dialog>

            </div>
        )
    }
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
