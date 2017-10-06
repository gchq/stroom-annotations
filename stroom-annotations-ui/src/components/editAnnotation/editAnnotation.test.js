import React from 'react';
import { shallow } from 'enzyme';

import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-15';

configure({ adapter: new Adapter() });

import EditAnnotation from './editAnnotation';

describe('EditAnnotation', () => {
    it('renders without crashing', () => {
        const annotation = {
            id: '1',
            content: 'some content',
            assignTo: 'This guy',
            lastUpdated: Date.now()
        }

        shallow(
            <EditAnnotation
                annotation={annotation}
                editAnnotation={() => {}}
                updateAnnotation={() => {}}
                removeAnnotation={() => {}}
                />
        );
    });
})